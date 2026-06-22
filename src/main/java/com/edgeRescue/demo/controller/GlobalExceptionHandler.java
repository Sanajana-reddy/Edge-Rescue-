package com.edgeRescue.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String KEY_STATUS = "status";
    private static final String KEY_ERROR = "error";
    private static final String KEY_MESSAGE = "message";

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> body = Map.of(
                KEY_STATUS, "BAD_REQUEST",
                KEY_ERROR, "Bad Request",
                KEY_MESSAGE, ex.getMessage() == null ? "Invalid request" : ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> body = Map.of(
                KEY_STATUS, "SERVICE_UNAVAILABLE",
                KEY_ERROR, "Service Unavailable",
                KEY_MESSAGE, "AI Triage Engine temporarily overloaded, using local manual dispatch routing."
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }
}

