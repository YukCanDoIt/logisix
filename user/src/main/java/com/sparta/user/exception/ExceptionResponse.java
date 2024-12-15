package com.sparta.user.exception;

public record ExceptionResponse(String message) {
    public String toWrite() {
        return "{" +
                "\"message\":" + "\"" + message + "\"" +
                "}";
    }
}