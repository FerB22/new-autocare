package com.autocare.booking_service.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleValidationErrors_DeberiaRetornar400_ConMapeoDeCampos() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError error = new FieldError("dto", "clienteId", "El cliente es obligatorio");
        
        when(bindingResult.getFieldErrors()).thenReturn(List.of(error));
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<Map<String, String>> respuesta = handler.handleValidationErrors(ex);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("El cliente es obligatorio", respuesta.getBody().get("clienteId"));
    }

    @Test
    void handleBusinessExceptions_CitaNoEncontrada_DeberiaRetornar404() {
        CitaNoEncontradaException ex = new CitaNoEncontradaException("Cita no existe");
        
        ResponseEntity<Map<String, String>> respuesta = handler.handleBusinessExceptions(ex);

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertEquals("Cita no existe", respuesta.getBody().get("error"));
    }

    @Test
    void handleBusinessExceptions_HorarioOcupado_DeberiaRetornar409() {
        HorarioOcupadoException ex = new HorarioOcupadoException("Horario no disponible");
        
        ResponseEntity<Map<String, String>> respuesta = handler.handleBusinessExceptions(ex);

        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertEquals("Horario no disponible", respuesta.getBody().get("error"));
    }

    @Test
    void handleRuntimeException_Generica_DeberiaRetornar500() {
        RuntimeException ex = new RuntimeException("Fallo en la base de datos");
        
        ResponseEntity<Map<String, String>> respuesta = handler.handleRuntimeException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respuesta.getStatusCode());
        assertTrue(respuesta.getBody().get("error").contains("Error interno del servicio"));
    }

    @Test
    void handleExternalServiceErrors_DeberiaRetornar404() {
        WebClientResponseException ex = mock(WebClientResponseException.class);
        
        ResponseEntity<Map<String, String>> respuesta = handler.handleExternalServiceErrors(ex);

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertTrue(respuesta.getBody().get("error").contains("Recurso no disponible en el sistema"));
    }
}