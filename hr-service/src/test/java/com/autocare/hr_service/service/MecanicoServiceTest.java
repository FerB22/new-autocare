package com.autocare.hr_service.service;

import com.autocare.hr_service.model.Mecanico;
import com.autocare.hr_service.repository.MecanicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MecanicoServiceTest {

    @Mock
    private MecanicoRepository mecanicoRepository;

    @InjectMocks
    private MecanicoService mecanicoService;

    private Mecanico mecanicoValido;

    @BeforeEach
    void setUp() {
        mecanicoValido = new Mecanico(
            1L, "12345678-9", "Juan", "Perez", "MOTOR", "+56912345678", true
        );
    }

    // ─── TEST 1 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Debe guardar un mecánico exitosamente (Estructura Given-When-Then)")
    void guardarMecanicoExitoso() {
        // GIVEN: No hay duplicados en la BD simulada
        when(mecanicoRepository.findAll()).thenReturn(List.of());
        when(mecanicoRepository.save(any(Mecanico.class))).thenReturn(mecanicoValido);

        // WHEN
        Mecanico resultado = mecanicoService.guardar(mecanicoValido);

        // THEN
        assertNotNull(resultado);
        assertTrue(resultado.isEstaDisponible());
        assertEquals("MOTOR", resultado.getEspecialidad());
        verify(mecanicoRepository, times(1)).save(any(Mecanico.class));
    }

    // ─── TEST 2 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Debe fallar al guardar si el RUT está duplicado")
    void fallarAlGuardarDocumentoDuplicado() {
        // GIVEN: La BD simulada ya tiene a este mecánico
        when(mecanicoRepository.findAll()).thenReturn(List.of(mecanicoValido));

        // WHEN / THEN
        Exception exception = assertThrows(RuntimeException.class, () -> {
            mecanicoService.guardar(mecanicoValido);
        });

        assertTrue(exception.getMessage().contains("Ya existe un mecánico con el documento"));
        verify(mecanicoRepository, never()).save(any());
    }

    // ─── TEST 3 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Debe fallar si la especialidad no está permitida")
    void fallarEspecialidadInvalida() {
        // GIVEN: Mecánico con especialidad inexistente
        mecanicoValido.setEspecialidad("AEROSPACIAL");

        // WHEN / THEN
        Exception exception = assertThrows(RuntimeException.class, () -> {
            mecanicoService.guardar(mecanicoValido);
        });

        assertTrue(exception.getMessage().contains("Especialidad inválida"));
    }

    // ─── TEST 4 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Regla de Negocio: Debe evitar eliminar a un mecánico ocupado")
    void evitarEliminarMecanicoOcupado() {
        // GIVEN: El mecánico está actualmente en una reparación
        mecanicoValido.setEstaDisponible(false);
        when(mecanicoRepository.findById(1L)).thenReturn(Optional.of(mecanicoValido));

        // WHEN / THEN
        Exception exception = assertThrows(RuntimeException.class, () -> {
            mecanicoService.eliminar(1L);
        });

        assertTrue(exception.getMessage().contains("está asignado a una orden"));
        verify(mecanicoRepository, never()).deleteById(anyLong());
    }

    // ─── TEST 5 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Debe cambiar disponibilidad de ocupado a disponible correctamente")
    void cambiarDisponibilidadExitoso() {
        // GIVEN: Mecánico ocupado
        mecanicoValido.setEstaDisponible(false);
        when(mecanicoRepository.findById(1L)).thenReturn(Optional.of(mecanicoValido));
        when(mecanicoRepository.save(any(Mecanico.class))).thenReturn(mecanicoValido);

        // WHEN
        Mecanico resultado = mecanicoService.cambiarDisponibilidad(1L, true);

        // THEN
        assertNotNull(resultado);
        verify(mecanicoRepository, times(1)).save(any(Mecanico.class));
    }

    // ─── TEST 6 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Regla de Negocio: No cambiar disponibilidad al mismo estado actual")
    void evitarCambiarAlMismoEstado() {
        // GIVEN: Mecánico ya está disponible (true)
        when(mecanicoRepository.findById(1L)).thenReturn(Optional.of(mecanicoValido));

        // WHEN / THEN: Intentamos ponerlo disponible de nuevo
        Exception exception = assertThrows(RuntimeException.class, () -> {
            mecanicoService.cambiarDisponibilidad(1L, true);
        });

        assertNotNull(exception.getMessage());
        verify(mecanicoRepository, never()).save(any());
    }
}