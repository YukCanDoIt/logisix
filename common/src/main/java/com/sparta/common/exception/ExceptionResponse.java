package com.sparta.common.exception;

public record ExceptionResponse(
        String message
) {
    public String toWrite() {
        return "{" +
                "\"message\":" + "\"" + message +"\"" +
                "}";
    }
}