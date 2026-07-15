package com.autocare.booking_service.controller;

import com.autocare.booking_service.assembler.CitaModelAssembler;
import com.autocare.booking_service.dto.CitaRequestDTO;
import com.autocare.booking_service.exception.CitaNoEncontradaException;
import com.autocare.booking_service.exception.HorarioOcupadoException;
import com.autocare.booking_service.model.Cita;
import com.autocare.booking_service.service.CitaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CitaController.class)
@Import(CitaModelAssembler.class)
class CitaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CitaService citaService;

    @Autowired
    private ObjectMapper objectMapper;

    private Cita citaBase;
    private CitaRequestDTO dto;

    @BeforeEach
    void setUp() {
        citaBase = new Cita();
        citaBase.setId(1L);
        citaBase.setClienteId(10L);
        citaBase.setVehiculoId(20L);
        citaBase.setFechaHora(LocalDateTime.of(2026, 12, 1, 10, 0));
        citaBase.setMotivo("Revisión técnica");
        citaBase.setEstado(Cita.EstadoCita.AGENDADA);

        dto = new CitaRequestDTO(10L, 20L, LocalDateTime.of(2026, 12, 1, 10, 0), "Revisión técnica");
    }

    // =====================================================
    // POST /api/reservas/citas  — crearCita()
    // =====================================================

    @Test
    @DisplayName("POST /citas - Crear cita exitosamente → 201 Created")
    void crearCita_DeberiaRetornar201() throws Exception {
        // GIVEN
        when(citaService.agendarCita(any(CitaRequestDTO.class))).thenReturn(citaBase);

        // WHEN & THEN
        mockMvc.perform(post("/api/reservas/citas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.motivo").value("Revisión técnica"))
                .andExpect(jsonPath("$.estado").value("AGENDADA"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.todas-las-citas.href").exists());
    }

    @Test
    @DisplayName("POST /citas - Rechazar por horario ocupado (RN-03) → 400 Bad Request")
    void crearCita_DeberiaRetornar400_PorHorarioOcupado() throws Exception {
        // GIVEN
        when(citaService.agendarCita(any(CitaRequestDTO.class)))
                .thenThrow(new HorarioOcupadoException("Ya existe una cita agendada en ese horario."));

        // WHEN & THEN
        mockMvc.perform(post("/api/reservas/citas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /citas - Rechazar por límite diario (RN-04) → 400 Bad Request")
    void crearCita_DeberiaRetornar400_PorLimiteDiario() throws Exception {
        // GIVEN
        when(citaService.agendarCita(any(CitaRequestDTO.class)))
                .thenThrow(new HorarioOcupadoException("Límite de 20 citas diarias alcanzado."));

        // WHEN & THEN
        mockMvc.perform(post("/api/reservas/citas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    // =====================================================
    // GET /api/reservas/citas/{id}  — obtenerCitaPorId()
    // =====================================================

    @Test
    @DisplayName("GET /citas/{id} - Obtener cita existente → 200 OK")
    void obtenerCitaPorId_DeberiaRetornar200() throws Exception {
        // GIVEN
        when(citaService.obtenerPorId(1L)).thenReturn(citaBase);

        // WHEN & THEN
        mockMvc.perform(get("/api/reservas/citas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.motivo").value("Revisión técnica"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.todas-las-citas.href").exists());
    }

    @Test
    @DisplayName("GET /citas/{id} - Cita no encontrada → 404 Not Found")
    void obtenerCitaPorId_DeberiaRetornar404_NoEncontrada() throws Exception {
        // GIVEN
        when(citaService.obtenerPorId(999L))
                .thenThrow(new CitaNoEncontradaException("No se encontró la cita con ID: 999"));

        // WHEN & THEN
        mockMvc.perform(get("/api/reservas/citas/999"))
                .andExpect(status().isNotFound());
    }

    // =====================================================
    // GET /api/reservas/citas  — obtenerTodas()
    // =====================================================

    @Test
    @DisplayName("GET /citas - Listar todas las citas → 200 OK con lista")
    void obtenerTodas_DeberiaRetornar200_ConLista() throws Exception {
        // GIVEN
        when(citaService.obtenerTodas()).thenReturn(List.of(citaBase));

        // WHEN & THEN
        mockMvc.perform(get("/api/reservas/citas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.citaList").isArray())
                .andExpect(jsonPath("$._embedded.citaList[0].id").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    @DisplayName("GET /citas - Lista vacía → 200 OK con colección vacía")
    void obtenerTodas_DeberiaRetornar200_ListaVacia() throws Exception {
        // GIVEN
        when(citaService.obtenerTodas()).thenReturn(Collections.emptyList());

        // WHEN & THEN
        mockMvc.perform(get("/api/reservas/citas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    // =====================================================
    // PUT /api/reservas/citas/{id}  — actualizarCita()
    // =====================================================

    @Test
    @DisplayName("PUT /citas/{id} - Actualizar cita exitosamente → 200 OK")
    void actualizarCita_DeberiaRetornar200() throws Exception {
        // GIVEN
        when(citaService.actualizarCita(eq(1L), any(CitaRequestDTO.class))).thenReturn(citaBase);

        // WHEN & THEN
        mockMvc.perform(put("/api/reservas/citas/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.todas-las-citas.href").exists());
    }

    @Test
    @DisplayName("PUT /citas/{id} - Cita no encontrada para actualizar → 404 Not Found")
    void actualizarCita_DeberiaRetornar404_NoEncontrada() throws Exception {
        // GIVEN
        when(citaService.actualizarCita(eq(999L), any(CitaRequestDTO.class)))
                .thenThrow(new CitaNoEncontradaException("No se encontró la cita con ID: 999"));

        // WHEN & THEN
        mockMvc.perform(put("/api/reservas/citas/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    // =====================================================
    // DELETE /api/reservas/citas/{id}  — eliminarCita()
    // =====================================================

    @Test
    @DisplayName("DELETE /citas/{id} - Eliminar cita exitosamente → 204 No Content")
    void eliminarCita_DeberiaRetornar204() throws Exception {
        // GIVEN
        doNothing().when(citaService).eliminarCita(1L);

        // WHEN & THEN
        mockMvc.perform(delete("/api/reservas/citas/1"))
                .andExpect(status().isNoContent());

        verify(citaService, times(1)).eliminarCita(1L);
    }

    @Test
    @DisplayName("DELETE /citas/{id} - Cita no encontrada para eliminar → 404 Not Found")
    void eliminarCita_DeberiaRetornar404_NoEncontrada() throws Exception {
        // GIVEN
        doThrow(new CitaNoEncontradaException("No se encontró la cita con ID: 999"))
                .when(citaService).eliminarCita(999L);

        // WHEN & THEN
        mockMvc.perform(delete("/api/reservas/citas/999"))
                .andExpect(status().isNotFound());
    }
}
