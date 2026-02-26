package com.example.demo1.services;

public class AmadeusApiException extends RuntimeException {
    private final int statusCode;

    public AmadeusApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}