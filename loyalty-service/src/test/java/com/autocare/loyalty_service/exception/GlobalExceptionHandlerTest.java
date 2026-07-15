package com.autocare.loyalty_service.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void handleRuntimeException_DeberiaRetornar404_ConMensaje() {
        RuntimeException ex = new RuntimeException("Perfil no encontrado");

        ResponseEntity<Map<String, Object>> respuesta = exceptionHandler.handleRuntimeException(ex);

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertEquals("Perfil no encontrado", respuesta.getBody().get("message"));
        assertEquals(404, respuesta.getBody().get("status"));
    }

    @Test
    void handleValidationException_DeberiaRetornar400_ConCamposConError() {
        // Simulamos el error complejo de validación de Spring
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        FieldError fieldError = new FieldError("dto", "clienteId", "El ID no puede ser nulo");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<Map<String, Object>> respuesta = exceptionHandler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals(400, respuesta.getBody().get("status"));
        
        // Verificamos que el mapa de errores contenga el campo que falló
        @SuppressWarnings("unchecked")
        Map<String, String> errores = (Map<String, String>) respuesta.getBody().get("campos");
        assertTrue(errores.containsKey("clienteId"));
        assertEquals("El ID no puede ser nulo", errores.get("clienteId"));
    }
}