package com.example.cs4076_server;

public class IncorrectActionException extends Exception{
    public IncorrectActionException() {
        super("Incorrect action chosen.");
    }
    public IncorrectActionException(String msg) {
        super(msg);
    }
}
