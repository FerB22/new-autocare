package com.autocare.booking_service.service;

import com.autocare.booking_service.dto.CitaRequestDTO;
import com.autocare.booking_service.model.Cita;
import com.autocare.booking_service.repository.CitaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CitaServiceTest {

    // ✅ CORRECCIÓN: nombre cambiado de "citaRepository" a "repository"
    // para coincidir con el campo en CitaService.java
    @Mock
    private CitaRepository repository;

    @InjectMocks
    private CitaService citaService;

    private CitaRequestDTO citaRequestValida;
    private Cita citaGuardada;

    @BeforeEach
    void setUp() {
        citaRequestValida = new CitaRequestDTO(
                1L,
                10L,
                LocalDateTime.of(2026, 10, 15, 10, 30),
                "Mantenimiento preventivo"
        );

        citaGuardada = new Cita();
        citaGuardada.setId(100L);
        citaGuardada.setClienteId(1L);
        citaGuardada.setVehiculoId(10L);
        citaGuardada.setFechaHora(LocalDateTime.of(2026, 10, 15, 10, 30));
        citaGuardada.setEstado(Cita.EstadoCita.AGENDADA);
    }

    @Test
    @DisplayName("Agendar Cita - Camino Feliz (Éxito)")
    void agendarCita_Exito() {
        // GIVEN
        when(repository.countByFecha(any())).thenReturn(5L);
        when(repository.existsByFechaAndHora(any(), any())).thenReturn(false);
        when(repository.save(any(Cita.class))).thenReturn(citaGuardada);

        // WHEN
        Cita resultado = citaService.agendarCita(citaRequestValida);

        // THEN
        assertNotNull(resultado);
        assertEquals(Cita.EstadoCita.AGENDADA, resultado.getEstado());
        assertEquals(100L, resultado.getId());
        verify(repository, times(1)).save(any(Cita.class));
    }

    @Test
    @DisplayName("Validación RN-04: Rechazar agendamiento si supera 20 citas diarias")
    void agendarCita_LanzaExcepcion_PorLimiteDiario() {
        // GIVEN
        when(repository.countByFecha(citaRequestValida.fechaHora().toLocalDate())).thenReturn(20L);

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            citaService.agendarCita(citaRequestValida);
        });

        assertTrue(exception.getMessage().contains("Límite de 20 citas diarias alcanzado"));
        verify(repository, never()).save(any(Cita.class));
    }

    @Test
    @DisplayName("Validación RN-03: Rechazar agendamiento por choque de horario")
    void agendarCita_LanzaExcepcion_PorChoqueDeHorario() {
        // GIVEN
        when(repository.countByFecha(citaRequestValida.fechaHora().toLocalDate())).thenReturn(10L);
        when(repository.existsByFechaAndHora(
                citaRequestValida.fechaHora().toLocalDate(),
                citaRequestValida.fechaHora().toLocalTime())).thenReturn(true);

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            citaService.agendarCita(citaRequestValida);
        });

        assertTrue(exception.getMessage().contains("Ya existe una cita agendada en ese horario"));
        verify(repository, never()).save(any(Cita.class));
    }

    @Test
    @DisplayName("Validación RN-05: Actualizar estado de cita exitosamente")
    void actualizarEstado_Exito() {
        // GIVEN
        when(repository.findById(100L)).thenReturn(Optional.of(citaGuardada));
        when(repository.save(any(Cita.class))).thenReturn(citaGuardada);

        // WHEN
        Cita resultado = citaService.cambiarEstado(100L, "CONFIRMADA");

        // THEN
        assertEquals(Cita.EstadoCita.CONFIRMADA, resultado.getEstado());
        verify(repository, times(1)).save(citaGuardada);
    }

    @Test
    @DisplayName("Buscar por ID - Lanza excepción si no existe")
    void buscarPorId_LanzaExcepcion_NoEncontrada() {
        // GIVEN
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(RuntimeException.class, () -> {
            citaService.obtenerPorId(999L);
        });
    }

    @Test
    @DisplayName("Obtener todas las citas")
    void obtenerTodas() {
        // GIVEN
        when(repository.findAll()).thenReturn(List.of(citaGuardada));

        // WHEN
        var resultado = citaService.obtenerTodas();

        // THEN
        assertFalse(resultado.isEmpty());
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Buscar por ID - Camino Feliz (Éxito)")
    void buscarPorId_Exito() {
        // GIVEN
        when(repository.findById(100L)).thenReturn(Optional.of(citaGuardada));

        // WHEN
        Cita resultado = citaService.obtenerPorId(100L);

        // THEN
        assertNotNull(resultado);
        assertEquals(100L, resultado.getId());
    }

    @Test
    @DisplayName("Actualizar cita exitosamente")
    void actualizarCita_Exito() {
        // GIVEN
        when(repository.findById(100L)).thenReturn(Optional.of(citaGuardada));
        when(repository.save(any(Cita.class))).thenReturn(citaGuardada);

        // WHEN
        Cita resultado = citaService.actualizarCita(100L, citaRequestValida);

        // THEN
        assertNotNull(resultado);
        assertEquals("Mantenimiento preventivo", resultado.getMotivo());
        verify(repository, times(1)).save(citaGuardada);
    }

    @Test
    @DisplayName("Eliminar cita exitosamente")
    void eliminarCita_Exito() {
        // GIVEN
        when(repository.findById(100L)).thenReturn(Optional.of(citaGuardada));
        doNothing().when(repository).delete(citaGuardada);

        // WHEN & THEN
        assertDoesNotThrow(() -> citaService.eliminarCita(100L));
        verify(repository, times(1)).delete(citaGuardada);
    }
}