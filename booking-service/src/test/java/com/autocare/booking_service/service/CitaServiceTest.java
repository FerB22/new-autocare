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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CitaServiceTest {

    @Mock
    private CitaRepository citaRepository;

    @InjectMocks
    private CitaService citaService;

    private CitaRequestDTO citaRequestValida;
    private Cita citaGuardada;

    @BeforeEach
    void setUp() {
        // CitaRequestDTO es un record, por lo que se inicializa pasando todos los argumentos al constructor
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
        // El modelo Cita unifica la fecha y hora en un LocalDateTime
        citaGuardada.setFechaHora(LocalDateTime.of(2026, 10, 15, 10, 30));
        // El modelo Cita utiliza el Enum EstadoCita en lugar de un String
        citaGuardada.setEstado(Cita.EstadoCita.AGENDADA);
    }

    @Test
    @DisplayName("Agendar Cita - Camino Feliz (Éxito)")
    void agendarCita_Exito() {
        // GIVEN: El día tiene solo 5 citas y el horario específico está libre
        when(citaRepository.countByFecha(any())).thenReturn(5L);
        when(citaRepository.existsByFechaAndHora(any(), any())).thenReturn(false);
        when(citaRepository.save(any(Cita.class))).thenReturn(citaGuardada);

        // WHEN
        Cita resultado = citaService.agendarCita(citaRequestValida);

        // THEN
        assertNotNull(resultado);
        assertEquals(Cita.EstadoCita.AGENDADA, resultado.getEstado());
        assertEquals(100L, resultado.getId());
        verify(citaRepository, times(1)).save(any(Cita.class));
    }

    @Test
    @DisplayName("Validación RN-04: Rechazar agendamiento si supera 20 citas diarias")
    void agendarCita_LanzaExcepcion_PorLimiteDiario() {
        // GIVEN
        when(citaRepository.countByFecha(citaRequestValida.fechaHora().toLocalDate())).thenReturn(20L);

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            citaService.agendarCita(citaRequestValida);
        });

        assertTrue(exception.getMessage().contains("Límite de 20 citas diarias alcanzado"));
        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    @DisplayName("Validación RN-03: Rechazar agendamiento por choque de horario")
    void agendarCita_LanzaExcepcion_PorChoqueDeHorario() {
        // GIVEN
        when(citaRepository.countByFecha(citaRequestValida.fechaHora().toLocalDate())).thenReturn(10L); 
        when(citaRepository.existsByFechaAndHora(citaRequestValida.fechaHora().toLocalDate(), citaRequestValida.fechaHora().toLocalTime())).thenReturn(true); 

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            citaService.agendarCita(citaRequestValida);
        });

        assertTrue(exception.getMessage().contains("Ya existe una cita agendada en ese horario"));
        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    @DisplayName("Validación RN-05: Actualizar estado de cita exitosamente")
    void actualizarEstado_Exito() {
        // GIVEN
        when(citaRepository.findById(100L)).thenReturn(Optional.of(citaGuardada));
        when(citaRepository.save(any(Cita.class))).thenReturn(citaGuardada);

        // WHEN
        // Recuerda que debes agregar el método cambiarEstado en tu CitaService
        Cita resultado = citaService.cambiarEstado(100L, "CONFIRMADA");

        // THEN
        // La validación ahora se hace contra el Enum en lugar del String
        assertEquals(Cita.EstadoCita.CONFIRMADA, resultado.getEstado());
        verify(citaRepository, times(1)).save(citaGuardada);
    }

    @Test
    @DisplayName("Buscar por ID - Lanza excepción si no existe")
    void buscarPorId_LanzaExcepcion_NoEncontrada() {
        // GIVEN
        when(citaRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(RuntimeException.class, () -> {
            citaService.obtenerPorId(999L);
        });
    }

    @Test
    @DisplayName("Obtener todas las citas")
    void obtenerTodas() {
        when(citaRepository.findAll()).thenReturn(java.util.List.of(citaGuardada));
        
        var resultado = citaService.obtenerTodas();
        
        assertFalse(resultado.isEmpty());
        verify(citaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Buscar por ID - Camino Feliz (Éxito)")
    void buscarPorId_Exito() {
        when(citaRepository.findById(100L)).thenReturn(Optional.of(citaGuardada));
        
        Cita resultado = citaService.obtenerPorId(100L);
        
        assertNotNull(resultado);
        assertEquals(100L, resultado.getId());
    }

    @Test
    @DisplayName("Actualizar cita exitosamente")
    void actualizarCita_Exito() {
        when(citaRepository.findById(100L)).thenReturn(Optional.of(citaGuardada));
        when(citaRepository.save(any(Cita.class))).thenReturn(citaGuardada);

        Cita resultado = citaService.actualizarCita(100L, citaRequestValida);

        assertNotNull(resultado);
        // Validamos que los datos se hayan mapeado (motivo se actualiza desde el DTO)
        assertEquals("Mantenimiento preventivo", resultado.getMotivo());
        verify(citaRepository, times(1)).save(citaGuardada);
    }

    @Test
    @DisplayName("Eliminar cita exitosamente")
    void eliminarCita_Exito() {
        when(citaRepository.findById(100L)).thenReturn(Optional.of(citaGuardada));
        doNothing().when(citaRepository).delete(citaGuardada);

        assertDoesNotThrow(() -> citaService.eliminarCita(100L));
        verify(citaRepository, times(1)).delete(citaGuardada);
    }
}