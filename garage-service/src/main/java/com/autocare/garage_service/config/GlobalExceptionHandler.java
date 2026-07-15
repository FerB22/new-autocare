// config/GlobalExceptionHandler.java
package com.autocare.garage_service.config;

import com.autocare.garage_service.exception.ClienteNoEncontradoException;
import com.autocare.garage_service.exception.VehiculoNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ClienteNoEncontradoException.class, VehiculoNoEncontradoException.class})
    public ResponseEntity<Map<String, String>> handleNotFoundExceptions(RuntimeException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(Map.of("error", ex.getMessage()));
    }
}