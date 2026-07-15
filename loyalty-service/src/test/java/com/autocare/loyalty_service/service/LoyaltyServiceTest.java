package com.autocare.loyalty_service.service;

import com.autocare.loyalty_service.dto.CrearPerfilDTO;
import com.autocare.loyalty_service.dto.TransaccionPuntosDTO;
import com.autocare.loyalty_service.model.PerfilLealtad;
import com.autocare.loyalty_service.repository.PerfilLealtadRepository;
import org.junit.jupiter.api.BeforeEach;
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
class LoyaltyServiceTest {
    @Mock
    private PerfilLealtadRepository repository;

    @InjectMocks
    private LoyaltyService loyaltyService;

    private PerfilLealtad perfilBase;

    @BeforeEach
    void setUp() {
        // Inicializamos un perfil base para reusar en las pruebas
        perfilBase = new PerfilLealtad(
                1L, 
                100L, 
                200, 
                PerfilLealtad.NivelLealtad.BRONCE, 
                LocalDateTime.now()
        );
    }

    @Test
    void obtenerPerfil_CuandoExiste_DeberiaRetornarPerfil() {
        when(repository.findByClienteId(100L)).thenReturn(Optional.of(perfilBase));

        PerfilLealtad resultado = loyaltyService.obtenerPerfil(100L);

        assertNotNull(resultado);
        assertEquals(100L, resultado.getClienteId());
        verify(repository, times(1)).findByClienteId(100L);
    }

    @Test
    void obtenerPerfil_CuandoNoExiste_DeberiaLanzarExcepcion() {
        when(repository.findByClienteId(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            loyaltyService.obtenerPerfil(999L);
        });

        assertEquals("Perfil de lealtad no encontrado para el cliente: 999", exception.getMessage());
    }

    @Test
    void inicializarPerfil_CuandoNoExiste_DeberiaCrearYGuardar() {
        CrearPerfilDTO dto = new CrearPerfilDTO(200L);
        when(repository.findByClienteId(200L)).thenReturn(Optional.empty());
        
        // Simulamos que al guardar, retorna el perfil con los valores iniciales por defecto
        PerfilLealtad perfilGuardado = new PerfilLealtad(2L, 200L, 0, PerfilLealtad.NivelLealtad.BRONCE, LocalDateTime.now());
        when(repository.save(any(PerfilLealtad.class))).thenReturn(perfilGuardado);

        PerfilLealtad resultado = loyaltyService.inicializarPerfil(dto);

        assertNotNull(resultado);
        assertEquals(0, resultado.getPuntosAcumulados());
        assertEquals(PerfilLealtad.NivelLealtad.BRONCE, resultado.getNivel());
        verify(repository).save(any(PerfilLealtad.class));
    }

    @Test
    void inicializarPerfil_CuandoYaExiste_DeberiaLanzarExcepcion() {
        CrearPerfilDTO dto = new CrearPerfilDTO(100L);
        when(repository.findByClienteId(100L)).thenReturn(Optional.of(perfilBase));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            loyaltyService.inicializarPerfil(dto);
        });

        assertEquals("El cliente ya tiene un perfil de lealtad.", exception.getMessage());
        verify(repository, never()).save(any(PerfilLealtad.class));
    }

    @Test
    void sumarPuntos_DeberiaSumarYActualizarNivelA_Plata() {
        // El perfil base tiene 200 puntos. Le sumamos 350 para que llegue a 550 (Rango PLATA: >= 500)
        TransaccionPuntosDTO dto = new TransaccionPuntosDTO(350);
        when(repository.findByClienteId(100L)).thenReturn(Optional.of(perfilBase));
        when(repository.save(any(PerfilLealtad.class))).thenReturn(perfilBase);

        PerfilLealtad resultado = loyaltyService.sumarPuntos(100L, dto);

        assertEquals(550, resultado.getPuntosAcumulados());
        assertEquals(PerfilLealtad.NivelLealtad.PLATA, resultado.getNivel());
        verify(repository).save(perfilBase);
    }
    
    @Test
    void sumarPuntos_DeberiaSumarYActualizarNivelA_VIP() {
        // Le sumamos 5000 puntos para que supere la barrera VIP (>= 5000)
        TransaccionPuntosDTO dto = new TransaccionPuntosDTO(5000);
        when(repository.findByClienteId(100L)).thenReturn(Optional.of(perfilBase));
        when(repository.save(any(PerfilLealtad.class))).thenReturn(perfilBase);

        PerfilLealtad resultado = loyaltyService.sumarPuntos(100L, dto);

        assertEquals(5200, resultado.getPuntosAcumulados());
        assertEquals(PerfilLealtad.NivelLealtad.VIP, resultado.getNivel());
    }

    @Test
    void canjearPuntos_CuandoHaySuficientes_DeberiaRestarPuntos() {
        // El perfil base tiene 200 puntos. Canjeamos 150.
        TransaccionPuntosDTO dto = new TransaccionPuntosDTO(150);
        when(repository.findByClienteId(100L)).thenReturn(Optional.of(perfilBase));
        when(repository.save(any(PerfilLealtad.class))).thenReturn(perfilBase);

        PerfilLealtad resultado = loyaltyService.canjearPuntos(100L, dto);

        assertEquals(50, resultado.getPuntosAcumulados());
        assertEquals(PerfilLealtad.NivelLealtad.BRONCE, resultado.getNivel());
        verify(repository).save(perfilBase);
    }

    @Test
    void canjearPuntos_CuandoSonInsuficientes_DeberiaLanzarExcepcion() {
        // El perfil base tiene 200 puntos. Intentamos canjear 500.
        TransaccionPuntosDTO dto = new TransaccionPuntosDTO(500);
        when(repository.findByClienteId(100L)).thenReturn(Optional.of(perfilBase));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            loyaltyService.canjearPuntos(100L, dto);
        });

        assertEquals("Puntos insuficientes para el canje.", exception.getMessage());
        // Verificamos que al fallar el canje, los puntos no se modifiquen accidentalmente ni se guarde
        assertEquals(200, perfilBase.getPuntosAcumulados());
        verify(repository, never()).save(any(PerfilLealtad.class));
    }

    @Test
    void sumarPuntos_DeberiaSumarYActualizarNivelA_Oro() {
        // El perfil base tiene 200 puntos. Le sumamos 1800 para llegar a 2000 (Rango ORO: >= 2000)
        TransaccionPuntosDTO dto = new TransaccionPuntosDTO(1800);
        when(repository.findByClienteId(100L)).thenReturn(Optional.of(perfilBase));
        when(repository.save(any(PerfilLealtad.class))).thenReturn(perfilBase);

        PerfilLealtad resultado = loyaltyService.sumarPuntos(100L, dto);

        assertEquals(2000, resultado.getPuntosAcumulados());
        assertEquals(PerfilLealtad.NivelLealtad.ORO, resultado.getNivel());
        verify(repository).save(perfilBase);
    }
}