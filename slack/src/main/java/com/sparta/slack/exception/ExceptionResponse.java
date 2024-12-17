package com.sparta.slack.exception;

public record ExceptionResponse(String message) {
    public String toWrite() {
        return "{" +
                "\"message\":" + "\"" + message + "\"" +
                "}";
    }
}