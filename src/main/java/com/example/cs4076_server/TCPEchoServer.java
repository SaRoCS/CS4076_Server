package com.example.cs4076_server;

import org.json.simple.JSONObject;

import java.io.*;
import java.net.*;
import java.time.LocalTime;

/**
 * @author razi
 */
public class TCPEchoServer {
    private static ServerSocket servSock;
    private static final int PORT = 1234;
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
                JSONObject res = new JSONObject();
                res.put("response", data.get("name").toString().toUpperCase());
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
} // finish the class
