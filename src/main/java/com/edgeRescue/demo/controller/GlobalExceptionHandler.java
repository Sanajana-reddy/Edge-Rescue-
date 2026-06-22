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
        ex.printStackTrace(); // Log the real error so we can diagnose
        String message = ex.getMessage() == null ? "Internal server error" : ex.getMessage();
        Map<String, String> body = Map.of(
                KEY_STATUS, "INTERNAL_ERROR",
                KEY_ERROR, "Internal Server Error",
                KEY_MESSAGE, message
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
