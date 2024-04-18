package com.example.cs4076_server;

import javafx.application.Platform;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.time.DayOfWeek;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

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
     * A thread-safe hashmap to store the scheduled classes
     */
    private static final ConcurrentHashMap<String, CopyOnWriteArrayList<ModuleWrapper>> schedule = new ConcurrentHashMap<>();
    /**
     * The server's socket
     */
    private static ServerSocket servSock;

    public static void main(String[] args) {
        // Initialize schedule with school days
        for (DayOfWeek day : DayOfWeek.values()) {
            schedule.put(day.toString(), new CopyOnWriteArrayList<>());
        }

        // To use javafx.concurrent, the JFX runtime must be started
        Platform.startup(() -> {
        });

        System.out.println("Opening port...\n");

        /*
            WARNING!!! This is just for demonstration purposes. You should store and retrieve the password in a secure way.
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

            // Pass the connection to a new thread
            Thread worker = new Thread(new ConnectionThread(link, schedule));
            worker.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}