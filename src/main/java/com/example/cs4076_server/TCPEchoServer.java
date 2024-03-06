package com.example.cs4076_server;

import org.json.simple.JSONObject;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * @author razi
 */
public class TCPEchoServer {
    private static final int PORT = 1234;
    private static final ArrayList<ModuleWrapper> moduleArray = new ArrayList<ModuleWrapper>();
    private static ServerSocket servSock;
    private static int clientConnections = 0;

    public static void main(String[] args) {
        System.out.println("Opening port...\n");
        try {
            servSock = new ServerSocket(PORT);      //Step 1.
        } catch (IOException e) {
            System.out.println("Unable to attach to port!");
            System.exit(1);
        }

        do {
            run();
        } while (true);

    }

    private static void run() {
        Socket link = null;                        //Step 2.
        try {
            link = servSock.accept();               //Step 2.
            clientConnections++;
            while (true) {
                ObjectInputStream in = new ObjectInputStream(link.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(link.getOutputStream());

                JSONObject obj = (JSONObject) in.readObject();
                JSONObject data = (JSONObject) obj.get("data");
                String resMsg;

                try {
                    resMsg = processMsg((String) obj.get("action"), data);
                } catch (IncorrectActionException e) {
                    resMsg = e.getMessage();
                }

                JSONObject res = new JSONObject();
                res.put("response", resMsg);
                out.writeObject(res);
            }
        } catch (EOFException e) {
            // Client disconnect
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                System.out.println("\n* Closing connection... *");
                link.close();                    //Step 5.
            } catch (IOException e) {
                System.out.println("Unable to disconnect!");
                System.exit(1);
            }
        }
    } // finish run method

    private static String processMsg(String action, JSONObject data) throws IncorrectActionException {
        String response = "";
        if (action.equals("Display Schedule")) {
            //displaySchedule();
            response = "Schedule displayed";
        } else {
            if (action.equals("Add Class") || action.equals("Remove Class")) {
                if (data == null) {
                    throw new IncorrectActionException("Cannot " + action + ". Missing data.");
                }

                ModuleWrapper curModule = new ModuleWrapper(data);

                if (curModule.getStartTime().isAfter(curModule.getEndTime()) || curModule.getStartTime().equals(curModule.getEndTime())) {
                    throw new IncorrectActionException("Start time must be before end time.");
                }

                if (action.equals("Add Class")) {
                    for (int i = 0; i < moduleArray.size(); i++) {
                        if (moduleArray.get(i).equals(curModule)) {
                            response = "Cannot add class. Class already exists.";
                        } else if (moduleArray.get(i).overlaps((curModule))) {
                            response = "Cannot add class. Class overlaps with existing class.";
                        }
                    }
                    moduleArray.add(curModule);
                    response = "Class successfully added.";
                } else if (action.equals("Remove Class")) {
                    response = "Cannot remove class. Class doesn't exist";
                    for (int i = 0; i < moduleArray.size(); i++) {
                        if (moduleArray.get(i).equals(curModule)) {
                            moduleArray.remove(i);
                            response = "Class Removed";
                        }
                    }

                }
            } else {
                throw new IncorrectActionException("Unknown action.");
            }
        }
        return response;
    }
} // finish the class