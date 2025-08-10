package com.salah.taskmate.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public static final String ERROR = "error";
    public static final String MESSAGE = "message";
    public static final String TIMESTAMP = "timestamp";
    public static final String STATUS = "status";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex
                .getBindingResult()
                .getFieldErrors()
                .forEach(fieldError ->
                        errors.put(fieldError.getField(), fieldError.getDefaultMessage())
        );
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put(ERROR, "Registration Failed");
        response.put(MESSAGE, ex.getMessage());
        response.put(TIMESTAMP, LocalDateTime.now());
        response.put(STATUS, HttpStatus.CONFLICT.value());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUsernameNotFound(UsernameNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put(ERROR, "Login Failed");
        response.put(MESSAGE, "Invalid email or password");
        response.put(TIMESTAMP, LocalDateTime.now());
        response.put(STATUS, HttpStatus.UNAUTHORIZED.value());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}
