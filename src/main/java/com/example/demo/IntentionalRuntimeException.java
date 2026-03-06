package com.example.demo;

public class IntentionalRuntimeException extends RuntimeException {
    public IntentionalRuntimeException(String message) {
        super(message);
    }
}
