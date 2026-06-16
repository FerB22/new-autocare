package com.autocare.billing_service;

import com.autocare.billing_service.exception.RecursoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNoEncontrado_DeberiaRetornar404() {
        RecursoNoEncontradoException ex = new RecursoNoEncontradoException("Factura no encontrada");
        
        ResponseEntity<Map<String, String>> respuesta = handler.handleNoEncontrado(ex);

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertEquals("Factura no encontrada", respuesta.getBody().get("error"));
    }

    @Test
    void handleArgumentoInvalido_DeberiaRetornar400() {
        IllegalArgumentException ex = new IllegalArgumentException("ID nulo");
        
        ResponseEntity<Map<String, String>> respuesta = handler.handleArgumentoInvalido(ex);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("ID nulo", respuesta.getBody().get("error"));
    }
}