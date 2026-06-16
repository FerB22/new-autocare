package com.autocare.billing_service.controller;

import com.autocare.billing_service.dto.FacturaRequestDTO;
import com.autocare.billing_service.model.Factura;
import com.autocare.billing_service.service.FacturaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FacturaController.class)
class FacturaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FacturaService facturaService;

    @Autowired
    private ObjectMapper objectMapper;

    private Factura facturaBase;

    @BeforeEach
    void setUp() {
        facturaBase = new Factura(1L, 100L, new BigDecimal("50.0"), new BigDecimal("9.5"), new BigDecimal("59.5"), Factura.EstadoPago.PENDIENTE, LocalDateTime.now());
    }

    @Test
    void listar_DeberiaRetornarListaDeFacturas() throws Exception {
        when(facturaService.listarTodas()).thenReturn(List.of(facturaBase));
        
        mockMvc.perform(get("/api/facturacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void buscarPorId_CuandoExiste_DeberiaRetornar200() throws Exception {
        when(facturaService.buscarPorId(1L)).thenReturn(Optional.of(facturaBase));
        
        mockMvc.perform(get("/api/facturacion/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void buscarPorId_CuandoNoExiste_DeberiaRetornar404() throws Exception {
        when(facturaService.buscarPorId(999L)).thenReturn(Optional.empty());
        
        mockMvc.perform(get("/api/facturacion/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void buscarPorEstado_DeberiaRetornarFacturas() throws Exception {
        when(facturaService.buscarPorEstado(Factura.EstadoPago.PENDIENTE)).thenReturn(List.of(facturaBase));
        
        mockMvc.perform(get("/api/facturacion/estado/PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));
    }

    @Test
    void generar_ConDatosValidos_DeberiaRetornar201() throws Exception {
        FacturaRequestDTO dto = new FacturaRequestDTO(100L, new BigDecimal("50.0"), new BigDecimal("9.5"));
        when(facturaService.generar(any(FacturaRequestDTO.class))).thenReturn(facturaBase);

        mockMvc.perform(post("/api/facturacion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void pagarFactura_DeberiaRetornar200() throws Exception {
        when(facturaService.pagarFactura(1L)).thenReturn(facturaBase);
        
        mockMvc.perform(patch("/api/facturacion/1/pagar"))
                .andExpect(status().isOk());
    }

    @Test
    void anularFactura_DeberiaRetornar200() throws Exception {
        when(facturaService.anularFactura(1L)).thenReturn(facturaBase);
        
        mockMvc.perform(patch("/api/facturacion/1/anular"))
                .andExpect(status().isOk());
    }

    @Test
    void eliminar_DeberiaRetornar204() throws Exception {
        doNothing().when(facturaService).eliminar(1L);
        
        mockMvc.perform(delete("/api/facturacion/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void buscarPorId_CuandoIdEsNulo_DeberiaRetornarBadRequest() throws Exception {
        // GIVEN: Forzamos el comportamiento en el servicio mock
        when(facturaService.buscarPorId(null)).thenThrow(new IllegalArgumentException("El ID no puede ser nulo"));

        // WHEN & THEN: Invocamos el controlador enviando una cadena vacía o un espacio 
        // para obligar a Spring a entrar al ruteo de la variable pero fallar en la conversión de tipo
        mockMvc.perform(get("/api/facturacion/{id}", " "))
                .andExpect(status().isBadRequest());
    }
}