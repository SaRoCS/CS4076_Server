package com.example.cs4076_server;

import org.json.simple.JSONObject;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.*;
import java.security.cert.CertificateException;
import java.time.DayOfWeek;
import java.util.ArrayList;

/**
 * The TCP server application to manage scheduling classes
 *
 * @author Westin Gjervold
 * @author Samuel Schroeder
 */
public class TCPServer {
    /**
     * The port to listen on
     */
    private static final int PORT = 1234;
    /**
     * An array to store the scheduled classes
     */
    private static final ArrayList<ModuleWrapper> moduleArray = new ArrayList<>();
    /**
     * The server's socket
     */
    private static ServerSocket servSock;

    public static void main(String[] args) {

        System.out.println("Opening port...\n");

        /*
            WARNING!!! This is just for demonstration purposes. You store and retrieve the password in a secure way.
         */
        char[] password = "cs4076".toCharArray();

        createSSLSocket("server_keystore.jks", password);

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
                // Get I/O streams for sending and receiving objects
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
                        link.close();
                        break;
                    } catch (IOException e) {
                        System.out.println("Unable to disconnect!");
                        System.exit(1);
                    }
                }

            }
        } catch (SocketException e) {
            if (e.getMessage().equals("Connection reset")) {
                System.out.println("Client disconnected.");
            } else {
                e.printStackTrace();
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
            displaySchedule();
            response = "Schedule displayed";
        } else if (action.equals("Add Class") || action.equals("Remove Class")) {
            // Class data is required for adding and removing
            if (data == null) {
                throw new IncorrectActionException("Cannot " + action + ". Missing data.");
            }

            ModuleWrapper newModule = new ModuleWrapper(data);

            // Validate that the module ends after it starts
            if (newModule.getStartTime().isAfter(newModule.getEndTime()) || newModule.getStartTime().equals(newModule.getEndTime())) {
                return "Start time must be before end time.";
            }

            if (action.equals("Add Class")) {
                response = addClass(newModule);
            } else if (action.equals("Remove Class")) {
                response = removeClass(newModule);
            }
        } else {
            throw new IncorrectActionException("Unknown action.");
        }
        return response;
    }

    /**
     * Displays the created schedule in the terminal
     */
    private static void displaySchedule() {
        System.out.println("SCHEDULE:");
        System.out.print("Day         Start       End         Name        Room Number\n");
        System.out.println("-------------------------------------------------------------------");
        for (ModuleWrapper module : moduleArray) {
            System.out.printf("%-12s%-12s%-12s%-12s%-12s\n", module.getDayOfWeek(), module.getStartTime(), module.getEndTime(), module.getName(), module.getRoomNumber());
        }
        System.out.println();
    }

    /**
     * Creates an SSL socket
     *
     * @param keyStoreFile The path to the server's keystore file
     * @param password     The keystore password
     */
    private static void createSSLSocket(String keyStoreFile, char[] password) {
        SSLServerSocketFactory sslServerSocketFactory = null;

        try {
            // Load the server's key from the key store file into the key manager
            KeyStore keyStore = KeyStore.getInstance("JKS");
            InputStream inputStream = new FileInputStream(keyStoreFile);
            keyStore.load(inputStream, password);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, password);

            // Create SSL factory with the keys
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
            sslServerSocketFactory = sslContext.getServerSocketFactory();
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | KeyManagementException |
                 UnrecoverableKeyException | IOException e) {
            e.printStackTrace();
        }

        try {
            // Creat SSL socket
            servSock = sslServerSocketFactory.createServerSocket(PORT);
        } catch (NullPointerException e) {
            System.out.println("Unable to create SSL connection!");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Unable to attach to port!");
            System.exit(1);
        }
    }

    /**
     * Adds a module to the schedule, checking for clashes before adding
     *
     * @param newModule The module to add
     * @return The response message
     */
    private static String addClass(ModuleWrapper newModule) {
        String result = "";
        boolean canAdd = true;

        // Check each existing module to see if this new module already exists or clashes with an existing module
        for (ModuleWrapper moduleWrapper : moduleArray) {
            if (moduleWrapper.equals(newModule)) {
                result = "Cannot add class. Class already exists.";
                canAdd = false;
                break;
            } else if (moduleWrapper.overlaps((newModule))) {
                result = "Cannot add class. Class overlaps with existing class.";
                canAdd = false;
                break;
            }
        }

        if (canAdd) {
            moduleArray.add(newModule);

            // sort the array by day then time
            moduleArray.sort((o1, o2) -> {

                if (o1.equals(o2)) {
                    return 0;
                }

                int dayCompare = DayOfWeek.valueOf(o1.getDayOfWeek()).compareTo(DayOfWeek.valueOf(o2.getDayOfWeek()));
                if (dayCompare > 0) {
                    return 1;
                } else if (dayCompare < 0) {
                    return -1;
                } else {
                    if (o1.getStartTime().isBefore(o2.getStartTime())) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            });

            result = newModule.getName() + " at " + newModule.getStartTime() + " successfully added.";
        }
        return result;
    }

    /**
     * Removes an existing module from the schedule
     *
     * @param newModule The module to remove
     * @return The response message
     */
    private static String removeClass(ModuleWrapper newModule) {
        // Find the class and delete it if it exists
        String response = "Cannot remove class. Class doesn't exist";
        for (int i = 0; i < moduleArray.size(); i++) {
            if (moduleArray.get(i).equals(newModule)) {
                moduleArray.remove(i);
                response = "Time slot freed on " + newModule.getDayOfWeek() + " from " + newModule.getStartTime() + " to " + newModule.getEndTime() + " in " + newModule.getRoomNumber() + ".";
                break;
            }
        }
        return response;
    }
}