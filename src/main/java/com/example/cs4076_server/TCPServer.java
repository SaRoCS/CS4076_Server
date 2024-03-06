package com.example.cs4076_server;

import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * The TCP server application to manage scheduling classes
 */
public class TCPServer {
    /**
     * The port to listen on
     */
    private static final int PORT = 1234;
    /**
     * An array to store the scheduled classes
     */
    private static final ArrayList<ModuleWrapper> moduleArray = new ArrayList<ModuleWrapper>();
    /**
     * The server's socket
     */
    private static ServerSocket servSock;

    public static void main(String[] args) {
        // Create the TCP socket
        System.out.println("Opening port...\n");
        try {
            servSock = new ServerSocket(PORT);
        } catch (IOException e) {
            System.out.println("Unable to attach to port!");
            System.exit(1);
        }

        // Handle incoming connections
        do {
            run();
        } while (true);

    }

    /**
     * Handles client connections
     */
    private static void run() {
        Socket link = null;
        try {
            link = servSock.accept();

            while (true) {
                // Get I/O d streams for sending and receiving objects
                ObjectInputStream in = new ObjectInputStream(link.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(link.getOutputStream());

                // Read in the client's message
                JSONObject obj = (JSONObject) in.readObject();
                JSONObject data = (JSONObject) obj.get("data");
                String resMsg;

                // Determine the server's response
                try {
                    if (obj.get("action").equals("STOP")) {
                        resMsg = "TERMINATE";
                    } else {
                        resMsg = processMsg((String) obj.get("action"), data);
                    }
                } catch (IncorrectActionException e) {
                    resMsg = e.getMessage();
                }

                // Send the response
                JSONObject res = new JSONObject();
                res.put("response", resMsg);
                out.writeObject(res);

                // Close the connection if requested
                if (resMsg.equals("TERMINATE")) {
                    try {
                        System.out.println("\n* Closing connection... *");
                        link.close();
                        break;
                    } catch (IOException e) {
                        System.out.println("Unable to disconnect!");
                        System.exit(1);
                    }
                }

            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                System.out.println("\n* Closing connection... *");
                link.close();
            } catch (IOException e) {
                System.out.println("Unable to disconnect!");
                System.exit(1);
            }
        }
    }

    /**
     * Process client messages
     *
     * @param action The requested action
     * @param data   The given data to use with the action
     * @return The server's response message
     * @throws IncorrectActionException Thrown when an invalid action-data pair is received
     */
    private static String processMsg(String action, JSONObject data) throws IncorrectActionException {
        String response = "";
        if (action.equals("Display Schedule")) {
            //displaySchedule();
            response = "Schedule displayed";
        } else {
            if (action.equals("Add Class") || action.equals("Remove Class")) {
                // Class data is required for adding and removing
                if (data == null) {
                    throw new IncorrectActionException("Cannot " + action + ". Missing data.");
                }

                ModuleWrapper curModule = new ModuleWrapper(data);

                // Validate that the module ends after it starts
                if (curModule.getStartTime().isAfter(curModule.getEndTime()) || curModule.getStartTime().equals(curModule.getEndTime())) {
                    return "Start time must be before end time.";
                }

                if (action.equals("Add Class")) {
                    boolean canAdd = true;
                    // Check each existing module to see if this new module already exists or clashes with an existing module
                    for (int i = 0; i < moduleArray.size(); i++) {
                        if (moduleArray.get(i).equals(curModule)) {
                            response = "Cannot add class. Class already exists.";
                            canAdd = false;
                            break;
                        } else if (moduleArray.get(i).overlaps((curModule))) {
                            response = "Cannot add class. Class overlaps with existing class.";
                            canAdd = false;
                            break;
                        }
                    }
                    if (canAdd) {
                        moduleArray.add(curModule);
                        response = "Class successfully added.";
                    }
                } else if (action.equals("Remove Class")) {
                    // Find the class and delete it if it exists
                    response = "Cannot remove class. Class doesn't exist";
                    for (int i = 0; i < moduleArray.size(); i++) {
                        if (moduleArray.get(i).equals(curModule)) {
                            moduleArray.remove(i);
                            response = "Time slot freed on " + curModule.getDayOfWeek() + " from " + curModule.getStartTime() + " to " + curModule.getEndTime() + " in " + curModule.getRoomNumber() + ".";
                            break;
                        }
                    }
                }
            } else {
                throw new IncorrectActionException("Unknown action.");
            }
        }
        return response;
    }


}