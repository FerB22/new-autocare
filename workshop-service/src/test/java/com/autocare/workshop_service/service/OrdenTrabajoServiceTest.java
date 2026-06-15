package com.autocare.workshop_service.service;

import com.autocare.workshop_service.model.OrdenTrabajo;
import com.autocare.workshop_service.repository.OrdenTrabajoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrdenTrabajoServiceTest {

    @Mock
    private OrdenTrabajoRepository ordenTrabajoRepository;

    @InjectMocks
    private OrdenTrabajoService ordenTrabajoService;

    private OrdenTrabajo ordenMock;

    @BeforeEach
    void setUp() {
        ordenMock = new OrdenTrabajo();
        ordenMock.setId(1L);
        ordenMock.setVehiculoId(50L);
        // Corregido: INICIADA -> RECEPCIONADO
        ordenMock.setEstado(OrdenTrabajo.EstadoOrden.RECEPCIONADO);
    }

    @Test
    @DisplayName("Actualizar Estado a LISTO - Éxito (RN-09)")
    void actualizarEstado_Finalizada_Exito() {
        // GIVEN
        when(ordenTrabajoRepository.findById(1L)).thenReturn(Optional.of(ordenMock));
        when(ordenTrabajoRepository.save(any(OrdenTrabajo.class))).thenReturn(ordenMock);

        // WHEN
        // Corregido: FINALIZADA -> LISTO
        OrdenTrabajo resultado = ordenTrabajoService.actualizarEstado(1L, OrdenTrabajo.EstadoOrden.LISTO);

        // THEN
        assertNotNull(resultado);
        assertEquals(OrdenTrabajo.EstadoOrden.LISTO, resultado.getEstado());
        verify(ordenTrabajoRepository, times(1)).save(ordenMock);
    }

    @Test
    @DisplayName("Actualizar Estado - Lanza excepción si no existe la Orden")
    void actualizarEstado_LanzaExcepcion_NoEncontrada() {
        // GIVEN
        when(ordenTrabajoRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(RuntimeException.class, () -> {
            // Corregido: EN_PROCESO -> EN_PROGRESO
            ordenTrabajoService.actualizarEstado(999L, OrdenTrabajo.EstadoOrden.EN_PROGRESO);
        });

        verify(ordenTrabajoRepository, never()).save(any(OrdenTrabajo.class));
    }
}