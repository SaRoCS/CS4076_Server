package com.example.cs4076_server;

/**
 * Exception class to handle improperly formatted action requests
 */
public class IncorrectActionException extends Exception {
    public IncorrectActionException() {
        super("Incorrect action chosen.");
    }

    public IncorrectActionException(String msg) {
        super(msg);
    }
}
