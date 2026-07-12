package com.autocare.booking_service.service;

import com.autocare.booking_service.dto.CitaRequestDTO;
import com.autocare.booking_service.model.Cita;
import com.autocare.booking_service.repository.CitaRepository;
import com.autocare.booking_service.client.GarageClient;
import org.springframework.web.server.ResponseStatusException;
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

    @Mock
    private CitaRepository repository;

    @Mock
    private GarageClient garageClient;

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
        citaGuardada.setMotivo("Mantenimiento preventivo");
        citaGuardada.setEstado(Cita.EstadoCita.AGENDADA);
    }

    // =====================================================
    // agendarCita()
    // =====================================================

    @Test
    @DisplayName("Agendar Cita - Camino Feliz (Éxito)")
    void agendarCita_Exito() {
        // GIVEN
        when(garageClient.existeCliente(anyLong())).thenReturn(true);
        when(garageClient.existeVehiculo(anyLong())).thenReturn(true);
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
        when(garageClient.existeCliente(anyLong())).thenReturn(true);
        when(garageClient.existeVehiculo(anyLong())).thenReturn(true);
        when(repository.countByFecha(citaRequestValida.fechaHora().toLocalDate()))
                .thenReturn(20L);

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
        when(garageClient.existeCliente(anyLong())).thenReturn(true);
        when(garageClient.existeVehiculo(anyLong())).thenReturn(true);
        when(repository.countByFecha(citaRequestValida.fechaHora().toLocalDate()))
                .thenReturn(10L);
        when(repository.existsByFechaAndHora(
                citaRequestValida.fechaHora().toLocalDate(),
                citaRequestValida.fechaHora().toLocalTime()))
                .thenReturn(true);

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            citaService.agendarCita(citaRequestValida);
        });

        assertTrue(exception.getMessage().contains("Ya existe una cita agendada en ese horario"));
        verify(repository, never()).save(any(Cita.class));
    }

    @Test
    @DisplayName("Agendar Cita - Límite de citas es exactamente 19 (aún permitido)")
    void agendarCita_Exito_ConExactamente19CitasDelDia() {
        // GIVEN — 19 citas, todavía bajo el límite de 20
        when(garageClient.existeCliente(anyLong())).thenReturn(true);
        when(garageClient.existeVehiculo(anyLong())).thenReturn(true);
        when(repository.countByFecha(any())).thenReturn(19L);
        when(repository.existsByFechaAndHora(any(), any())).thenReturn(false);
        when(repository.save(any(Cita.class))).thenReturn(citaGuardada);

        // WHEN
        Cita resultado = citaService.agendarCita(citaRequestValida);

        // THEN
        assertNotNull(resultado);
        verify(repository, times(1)).save(any(Cita.class));
    }

    // =====================================================
    // agendarCita() - Validación de RN-01
    // =====================================================

    @Test
    @DisplayName("Validación RN-01: Rechazar agendamiento si el cliente no existe")
    void agendarCita_LanzaExcepcion_ClienteNoExiste() {
        // GIVEN
        when(garageClient.existeCliente(anyLong())).thenReturn(false);

        // WHEN & THEN
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            citaService.agendarCita(citaRequestValida);
        });

        assertTrue(exception.getMessage().contains("El cliente no existe en el sistema"));
        verify(repository, never()).save(any(Cita.class));
    }

    @Test
    @DisplayName("Validación RN-01: Rechazar agendamiento si el vehículo no existe")
    void agendarCita_LanzaExcepcion_VehiculoNoExiste() {
        // GIVEN
        when(garageClient.existeCliente(anyLong())).thenReturn(true);
        when(garageClient.existeVehiculo(anyLong())).thenReturn(false);

        // WHEN & THEN
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            citaService.agendarCita(citaRequestValida);
        });

        assertTrue(exception.getMessage().contains("El vehículo no existe en el sistema"));
        verify(repository, never()).save(any(Cita.class));
    }

    // =====================================================
    // obtenerPorId()
    // =====================================================

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
        verify(repository, times(1)).findById(100L);
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

        verify(repository, times(1)).findById(999L);
    }

    // =====================================================
    // obtenerTodas()
    // =====================================================

    @Test
    @DisplayName("Obtener todas las citas - Lista con resultados")
    void obtenerTodas_RetornaLista() {
        // GIVEN
        when(repository.findAll()).thenReturn(List.of(citaGuardada));

        // WHEN
        var resultado = citaService.obtenerTodas();

        // THEN
        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Obtener todas las citas - Lista vacía")
    void obtenerTodas_RetornaListaVacia() {
        // GIVEN
        when(repository.findAll()).thenReturn(List.of());

        // WHEN
        var resultado = citaService.obtenerTodas();

        // THEN
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(repository, times(1)).findAll();
    }

    // =====================================================
    // obtenerCitasPorVehiculo()  ← agrega este método en CitaService
    // =====================================================

    @Test
    @DisplayName("Obtener citas por vehículo - Retorna lista con citas")
    void obtenerCitasPorVehiculo_Exito() {
        // GIVEN
        when(repository.findByVehiculoId(10L)).thenReturn(List.of(citaGuardada));

        // WHEN
        List<Cita> resultado = citaService.obtenerCitasPorVehiculo(10L);

        // THEN
        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).getVehiculoId());
        verify(repository, times(1)).findByVehiculoId(10L);
    }

    @Test
    @DisplayName("Obtener citas por vehículo - Retorna lista vacía si no tiene citas")
    void obtenerCitasPorVehiculo_RetornaListaVacia() {
        // GIVEN
        when(repository.findByVehiculoId(99L)).thenReturn(List.of());

        // WHEN
        List<Cita> resultado = citaService.obtenerCitasPorVehiculo(99L);

        // THEN
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(repository, times(1)).findByVehiculoId(99L);
    }

    // =====================================================
    // cambiarEstado()
    // =====================================================

    @Test
    @DisplayName("Cambiar estado - Camino Feliz (Éxito)")
    void cambiarEstado_Exito() {
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
    @DisplayName("Cambiar estado - Lanza excepción si cita no existe")
    void cambiarEstado_LanzaExcepcion_NoEncontrada() {
        // GIVEN
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(RuntimeException.class, () -> {
            citaService.cambiarEstado(999L, "CONFIRMADA");
        });

        verify(repository, never()).save(any(Cita.class));
    }

    // =====================================================
    // actualizarCita()
    // =====================================================

    @Test
    @DisplayName("Actualizar cita - Camino Feliz (Éxito)")
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
    @DisplayName("Actualizar cita - Lanza excepción si cita no existe")
    void actualizarCita_LanzaExcepcion_NoEncontrada() {
        // GIVEN
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(RuntimeException.class, () -> {
            citaService.actualizarCita(999L, citaRequestValida);
        });

        verify(repository, never()).save(any(Cita.class));
    }

    // =====================================================
    // eliminarCita()
    // =====================================================

    @Test
    @DisplayName("Eliminar cita - Camino Feliz (Éxito)")
    void eliminarCita_Exito() {
        // GIVEN
        when(repository.findById(100L)).thenReturn(Optional.of(citaGuardada));
        doNothing().when(repository).delete(citaGuardada);

        // WHEN & THEN
        assertDoesNotThrow(() -> citaService.eliminarCita(100L));
        verify(repository, times(1)).delete(citaGuardada);
    }

    @Test
    @DisplayName("Eliminar cita - Lanza excepción si cita no existe")
    void eliminarCita_LanzaExcepcion_NoEncontrada() {
        // GIVEN
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(RuntimeException.class, () -> {
            citaService.eliminarCita(999L);
        });

        verify(repository, never()).delete(any(Cita.class));
    }
}