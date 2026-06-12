package com.autocare.billing_service.service;

import com.autocare.billing_service.dto.FacturaRequestDTO;
import com.autocare.billing_service.model.Factura;
import com.autocare.billing_service.repository.FacturaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FacturaServiceTest {

    @Mock
    private FacturaRepository facturaRepository;

    @InjectMocks
    private FacturaService facturaService;

    private Factura facturaExistente;
    private FacturaRequestDTO requestValido;

    @BeforeEach
    void setUp() {
        facturaExistente = new Factura(
            1L, 100L,
            new BigDecimal("50.0"),
            new BigDecimal("9.5"),
            new BigDecimal("59.5"),
            Factura.EstadoPago.PENDIENTE,
            LocalDateTime.now()
        );
        requestValido = new FacturaRequestDTO(100L, new BigDecimal("50.0"), new BigDecimal("9.5"));
    }

    // ─── TEST 1 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Debe generar una factura calculando el total correctamente")
    void generarFacturaExitosamente() {
        // GIVEN: No existe factura previa para la orden 100
        when(facturaRepository.findByOrdenTrabajoId(100L)).thenReturn(Optional.empty());
        when(facturaRepository.save(any(Factura.class))).thenReturn(facturaExistente);

        // WHEN
        Factura resultado = facturaService.generar(requestValido);

        // THEN
        assertNotNull(resultado);
        assertEquals(Factura.EstadoPago.PENDIENTE, resultado.getEstado());
        assertEquals(new BigDecimal("59.5"), resultado.getTotal());
        verify(facturaRepository, times(1)).save(any(Factura.class));
    }

    // ─── TEST 2 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Regla de Negocio: No puede facturar la misma orden dos veces")
    void evitarDobleFacturacion() {
        // GIVEN: La BD simulada ya tiene una factura para la orden 100
        when(facturaRepository.findByOrdenTrabajoId(100L)).thenReturn(Optional.of(facturaExistente));

        // WHEN / THEN
        Exception exception = assertThrows(RuntimeException.class, () -> {
            facturaService.generar(requestValido);
        });

        // Mensaje exacto del service: "Ya existe una factura para la orden: 100..."
        assertTrue(exception.getMessage().contains("Ya existe una factura para la orden"));
        verify(facturaRepository, never()).save(any());
    }

    // ─── TEST 3 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Regla de Negocio: No cobrar monto cero o negativo")
    void evitarFacturarMontoCero() {
        // GIVEN: subtotal = 0, la validación de duplicado debe pasar primero
        FacturaRequestDTO requestCero = new FacturaRequestDTO(101L, BigDecimal.ZERO, BigDecimal.ZERO);
        when(facturaRepository.findByOrdenTrabajoId(101L)).thenReturn(Optional.empty());

        // WHEN / THEN
        Exception exception = assertThrows(RuntimeException.class, () -> {
            facturaService.generar(requestCero);
        });

        // Mensaje exacto del service: "No se puede generar una factura con monto $0 o negativo."
        assertTrue(exception.getMessage().contains("No se puede generar una factura con monto $0"));
        verify(facturaRepository, never()).save(any());
    }

    // ─── TEST 4 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Debe pagar una factura en estado PENDIENTE exitosamente")
    void pagarFacturaPendiente() {
        // GIVEN
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(facturaExistente));
        when(facturaRepository.save(any(Factura.class))).thenReturn(facturaExistente);

        // WHEN
        Factura resultado = facturaService.pagarFactura(1L);

        // THEN
        assertNotNull(resultado);
        verify(facturaRepository, times(1)).save(any(Factura.class));
    }

    // ─── TEST 5 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Regla de Negocio: No pagar una factura ANULADA")
    void evitarPagarFacturaAnulada() {
        // GIVEN: La factura ya está anulada
        facturaExistente.setEstado(Factura.EstadoPago.ANULADA);
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(facturaExistente));

        // WHEN / THEN
        Exception exception = assertThrows(RuntimeException.class, () -> {
            facturaService.pagarFactura(1L);
        });

        // Mensaje exacto del service: "La factura está ANULADA y no puede ser pagada."
        assertTrue(exception.getMessage().contains("ANULADA"));
        verify(facturaRepository, never()).save(any());
    }

    // ─── TEST 6 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Regla de Negocio: No anular una factura ya PAGADA")
    void evitarAnularFacturaPagada() {
        // GIVEN: La factura ya fue pagada
        facturaExistente.setEstado(Factura.EstadoPago.PAGADA);
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(facturaExistente));

        // WHEN / THEN
        Exception exception = assertThrows(RuntimeException.class, () -> {
            facturaService.anularFactura(1L);
        });

        // Mensaje exacto del service: "No se puede anular una factura que ya fue PAGADA."
        assertTrue(exception.getMessage().contains("PAGADA"));
        verify(facturaRepository, never()).save(any());
    }

    // ─── TEST 7 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Regla de Negocio: No eliminar una factura PAGADA")
    void evitarEliminarFacturaPagada() {
        // GIVEN
        facturaExistente.setEstado(Factura.EstadoPago.PAGADA);
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(facturaExistente));

        // WHEN / THEN
        Exception exception = assertThrows(RuntimeException.class, () -> {
            facturaService.eliminar(1L);
        });

        // Mensaje exacto del service: "No se puede eliminar una factura PAGADA..."
        assertTrue(exception.getMessage().contains("No se puede eliminar una factura PAGADA"));
        verify(facturaRepository, never()).deleteById(anyLong());
    }
}