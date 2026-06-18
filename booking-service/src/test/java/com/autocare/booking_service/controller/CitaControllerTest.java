package com.autocare.booking_service.controller;

import com.autocare.booking_service.dto.CitaRequestDTO;
import com.autocare.booking_service.model.Cita;
import com.autocare.booking_service.service.CitaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CitaController.class)
class CitaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
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

    @Test
    void crearCita_DeberiaRetornar201() throws Exception {
        when(citaService.agendarCita(any(CitaRequestDTO.class))).thenReturn(citaBase);

        mockMvc.perform(post("/api/reservas/citas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void obtenerCitaPorId_DeberiaRetornar200() throws Exception {
        when(citaService.obtenerPorId(1L)).thenReturn(citaBase);

        mockMvc.perform(get("/api/reservas/citas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void obtenerTodas_DeberiaRetornar200() throws Exception {
        when(citaService.obtenerTodas()).thenReturn(List.of(citaBase));

        mockMvc.perform(get("/api/reservas/citas"))
                .andExpect(status().isOk());
    }

    @Test
    void actualizarCita_DeberiaRetornar200() throws Exception {
        when(citaService.actualizarCita(eq(1L), any(CitaRequestDTO.class))).thenReturn(citaBase);

        mockMvc.perform(put("/api/reservas/citas/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void eliminarCita_DeberiaRetornar204() throws Exception {
        doNothing().when(citaService).eliminarCita(1L);

        mockMvc.perform(delete("/api/reservas/citas/1"))
                .andExpect(status().isNoContent());
    }
}