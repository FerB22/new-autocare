package com.autocare.booking_service.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    // =====================================================
    // handleValidationErrors() — MethodArgumentNotValidException → 400
    // =====================================================

    @Test
    @DisplayName("Validación: campo inválido retorna 400 con mensaje del campo")
    void handleValidationErrors_DeberiaRetornar400_ConMapeoDeCampos() {
        // GIVEN
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError error = new FieldError("dto", "clienteId", "El cliente es obligatorio");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(error));
        when(ex.getBindingResult()).thenReturn(bindingResult);

        // WHEN
        ResponseEntity<Map<String, String>> respuesta = handler.handleValidationErrors(ex);

        // THEN
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("El cliente es obligatorio", respuesta.getBody().get("clienteId"));
    }

    @Test
    @DisplayName("Validación: múltiples campos inválidos retorna 400 con todos los mensajes")
    void handleValidationErrors_DeberiaRetornar400_ConMultiplesCampos() {
        // GIVEN
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError error1 = new FieldError("dto", "clienteId", "El cliente es obligatorio");
        FieldError error2 = new FieldError("dto", "motivo", "El motivo no puede estar vacío");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(error1, error2));
        when(ex.getBindingResult()).thenReturn(bindingResult);

        // WHEN
        ResponseEntity<Map<String, String>> respuesta = handler.handleValidationErrors(ex);

        // THEN
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("El cliente es obligatorio", respuesta.getBody().get("clienteId"));
        assertEquals("El motivo no puede estar vacío", respuesta.getBody().get("motivo"));
        assertEquals(2, respuesta.getBody().size());
    }

    @Test
    @DisplayName("Validación: sin errores de campo retorna 400 con body vacío")
    void handleValidationErrors_DeberiaRetornar400_SinErrores() {
        // GIVEN
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());
        when(ex.getBindingResult()).thenReturn(bindingResult);

        // WHEN
        ResponseEntity<Map<String, String>> respuesta = handler.handleValidationErrors(ex);

        // THEN
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertTrue(respuesta.getBody().isEmpty());
    }

    // =====================================================
    // handleBusinessExceptions() — excepciones de negocio
    // CitaNoEncontradaException    → 404
    // HorarioOcupadoException      → 409
    // VehiculoYaAgendadoException  → 409
    // =====================================================

    @Test
    @DisplayName("Negocio: CitaNoEncontradaException retorna 404 con mensaje correcto")
    void handleBusinessExceptions_CitaNoEncontrada_DeberiaRetornar404() {
        // GIVEN
        CitaNoEncontradaException ex = new CitaNoEncontradaException("Cita no existe");

        // WHEN
        ResponseEntity<Map<String, String>> respuesta = handler.handleBusinessExceptions(ex);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertEquals("Cita no existe", respuesta.getBody().get("error"));
    }

    @Test
    @DisplayName("Negocio: HorarioOcupadoException retorna 409 con mensaje correcto")
    void handleBusinessExceptions_HorarioOcupado_DeberiaRetornar409() {
        // GIVEN
        HorarioOcupadoException ex = new HorarioOcupadoException("Horario no disponible");

        // WHEN
        ResponseEntity<Map<String, String>> respuesta = handler.handleBusinessExceptions(ex);

        // THEN
        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertEquals("Horario no disponible", respuesta.getBody().get("error"));
    }

    @Test
    @DisplayName("Negocio: VehiculoYaAgendadoException retorna 409 con mensaje correcto")
    void handleBusinessExceptions_VehiculoYaAgendado_DeberiaRetornar409() {
        // GIVEN
        VehiculoYaAgendadoException ex = new VehiculoYaAgendadoException("El vehículo ya tiene una cita agendada");

        // WHEN
        ResponseEntity<Map<String, String>> respuesta = handler.handleBusinessExceptions(ex);

        // THEN
        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertEquals("El vehículo ya tiene una cita agendada", respuesta.getBody().get("error"));
    }

    @Test
    @DisplayName("Negocio: CitaNoEncontradaException preserva el mensaje original exacto")
    void handleBusinessExceptions_CitaNoEncontrada_MensajeExacto() {
        // GIVEN
        String mensajeEsperado = "No se encontró la cita con ID: 999";
        CitaNoEncontradaException ex = new CitaNoEncontradaException(mensajeEsperado);

        // WHEN
        ResponseEntity<Map<String, String>> respuesta = handler.handleBusinessExceptions(ex);

        // THEN
        assertEquals(mensajeEsperado, respuesta.getBody().get("error"));
    }

    @Test
    @DisplayName("Negocio: respuesta siempre contiene la clave 'error' en el body")
    void handleBusinessExceptions_BodySiempreContieneClaveError() {
        // GIVEN
        HorarioOcupadoException ex = new HorarioOcupadoException("Límite superado");

        // WHEN
        ResponseEntity<Map<String, String>> respuesta = handler.handleBusinessExceptions(ex);

        // THEN
        assertNotNull(respuesta.getBody());
        assertTrue(respuesta.getBody().containsKey("error"));
    }

    // =====================================================
    // handleRuntimeException() — RuntimeException genérica → 500
    // =====================================================

    @Test
    @DisplayName("Error genérico: RuntimeException retorna 500")
    void handleRuntimeException_Generica_DeberiaRetornar500() {
        // GIVEN
        RuntimeException ex = new RuntimeException("Fallo en la base de datos");

        // WHEN
        ResponseEntity<Map<String, String>> respuesta = handler.handleRuntimeException(ex);

        // THEN
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respuesta.getStatusCode());
        assertTrue(respuesta.getBody().get("error").contains("Error interno del servicio"));
    }

    @Test
    @DisplayName("Error genérico: no expone el mensaje técnico al cliente")
    void handleRuntimeException_NoExponeMensajeTecnico() {
        // GIVEN — mensaje técnico con detalles internos
        RuntimeException ex = new RuntimeException("Connection refused: postgres:5432");

        // WHEN
        ResponseEntity<Map<String, String>> respuesta = handler.handleRuntimeException(ex);

        // THEN — el mensaje al cliente NO debe contener detalles técnicos
        String mensajeCliente = respuesta.getBody().get("error");
        assertFalse(mensajeCliente.contains("postgres"));
        assertFalse(mensajeCliente.contains("5432"));
        assertFalse(mensajeCliente.contains("Connection refused"));
    }

    @Test
    @DisplayName("Error genérico: body contiene la clave 'error'")
    void handleRuntimeException_BodyContieneClaveError() {
        // GIVEN
        RuntimeException ex = new RuntimeException("Cualquier error");

        // WHEN
        ResponseEntity<Map<String, String>> respuesta = handler.handleRuntimeException(ex);

        // THEN
        assertNotNull(respuesta.getBody());
        assertTrue(respuesta.getBody().containsKey("error"));
    }

    // =====================================================
    // handleExternalServiceErrors() — WebClientResponseException → 404
    // =====================================================

    @Test
    @DisplayName("Servicio externo: WebClientResponseException retorna 404")
    void handleExternalServiceErrors_DeberiaRetornar404() {
        // GIVEN
        WebClientResponseException ex = mock(WebClientResponseException.class);

        // WHEN
        ResponseEntity<Map<String, String>> respuesta = handler.handleExternalServiceErrors(ex);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertTrue(respuesta.getBody().get("error").contains("Recurso no disponible en el sistema"));
    }

    @Test
    @DisplayName("Servicio externo: mensaje indica al cliente que verifique vehículo o cliente")
    void handleExternalServiceErrors_MensajeOrientaAlCliente() {
        // GIVEN
        WebClientResponseException ex = mock(WebClientResponseException.class);

        // WHEN
        ResponseEntity<Map<String, String>> respuesta = handler.handleExternalServiceErrors(ex);

        // THEN
        String mensaje = respuesta.getBody().get("error");
        assertTrue(mensaje.contains("vehículo") || mensaje.contains("cliente"));
    }

    @Test
    @DisplayName("Servicio externo: body no es nulo y contiene clave 'error'")
    void handleExternalServiceErrors_BodyNoEsNulo() {
        // GIVEN
        WebClientResponseException ex = mock(WebClientResponseException.class);

        // WHEN
        ResponseEntity<Map<String, String>> respuesta = handler.handleExternalServiceErrors(ex);

        // THEN
        assertNotNull(respuesta.getBody());
        assertTrue(respuesta.getBody().containsKey("error"));
    }

    // =====================================================
    // Pruebas de las clases de excepción personalizadas
    // =====================================================

    @Test
    @DisplayName("CitaNoEncontradaException - Hereda de RuntimeException")
    void citaNoEncontradaException_EsRuntimeException() {
        // GIVEN & WHEN
        CitaNoEncontradaException ex = new CitaNoEncontradaException("mensaje");

        // THEN
        assertInstanceOf(RuntimeException.class, ex);
        assertEquals("mensaje", ex.getMessage());
    }

    @Test
    @DisplayName("HorarioOcupadoException - Hereda de RuntimeException")
    void horarioOcupadoException_EsRuntimeException() {
        // GIVEN & WHEN
        HorarioOcupadoException ex = new HorarioOcupadoException("mensaje");

        // THEN
        assertInstanceOf(RuntimeException.class, ex);
        assertEquals("mensaje", ex.getMessage());
    }

    @Test
    @DisplayName("VehiculoYaAgendadoException - Hereda de RuntimeException")
    void vehiculoYaAgendadoException_EsRuntimeException() {
        // GIVEN & WHEN
        VehiculoYaAgendadoException ex = new VehiculoYaAgendadoException("mensaje");

        // THEN
        assertInstanceOf(RuntimeException.class, ex);
        assertEquals("mensaje", ex.getMessage());
    }
}
