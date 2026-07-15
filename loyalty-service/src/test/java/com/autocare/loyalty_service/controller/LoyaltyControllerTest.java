package com.autocare.loyalty_service.controller;

import com.autocare.loyalty_service.dto.CrearPerfilDTO;
import com.autocare.loyalty_service.dto.TransaccionPuntosDTO;
import com.autocare.loyalty_service.model.PerfilLealtad;
import com.autocare.loyalty_service.service.LoyaltyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoyaltyController.class)
class LoyaltyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Falsificamos el servicio para que el controlador crea que está haciendo cálculos reales
    @MockitoBean
    private LoyaltyService loyaltyService;

    @Autowired
    private ObjectMapper objectMapper;

    private PerfilLealtad perfilBase;

    @BeforeEach
    void setUp() {
        perfilBase = new PerfilLealtad(1L, 100L, 200, PerfilLealtad.NivelLealtad.BRONCE, LocalDateTime.now());
    }

    @Test
    void consultarPerfil_DeberiaRetornar200_Y_DatosDelPerfil() throws Exception {
        when(loyaltyService.obtenerPerfil(100L)).thenReturn(perfilBase);

        mockMvc.perform(get("/api/lealtad/cliente/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value(100))
                .andExpect(jsonPath("$.puntosAcumulados").value(200));
    }

    @Test
    void crearPerfil_DeberiaRetornar201_Y_PerfilCreado() throws Exception {
        CrearPerfilDTO dto = new CrearPerfilDTO(100L);
        when(loyaltyService.inicializarPerfil(any(CrearPerfilDTO.class))).thenReturn(perfilBase);

        mockMvc.perform(post("/api/lealtad/cliente")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clienteId").value(100));
    }

    @Test
    void agregarPuntos_DeberiaRetornar200() throws Exception {
        TransaccionPuntosDTO dto = new TransaccionPuntosDTO(50);
        when(loyaltyService.sumarPuntos(eq(100L), any(TransaccionPuntosDTO.class))).thenReturn(perfilBase);

        mockMvc.perform(post("/api/lealtad/cliente/100/sumar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void restarPuntos_DeberiaRetornar200() throws Exception {
        TransaccionPuntosDTO dto = new TransaccionPuntosDTO(150);
        when(loyaltyService.canjearPuntos(eq(100L), any(TransaccionPuntosDTO.class))).thenReturn(perfilBase);

        mockMvc.perform(post("/api/lealtad/cliente/100/canjear")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }
}