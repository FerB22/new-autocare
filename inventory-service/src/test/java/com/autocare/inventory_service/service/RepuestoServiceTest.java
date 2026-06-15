package com.autocare.inventory_service.service;

import com.autocare.inventory_service.dto.RepuestoRequestDTO;
import com.autocare.inventory_service.dto.StockReductionDTO;
import com.autocare.inventory_service.model.Repuesto;
import com.autocare.inventory_service.repository.RepuestoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RepuestoServiceTest {

    @Mock
    private RepuestoRepository repuestoRepository;

    @InjectMocks
    private RepuestoService repuestoService;

    private Repuesto repuestoMock;
    private RepuestoRequestDTO repuestoRequestDTO;

    @BeforeEach
    void setUp() {
        // Configuramos una entidad Repuesto de prueba
        repuestoMock = new Repuesto(
                1L,
                "FIL-ACE-001",
                "Filtro de Aceite",
                "Filtro genérico para motor",
                new BigDecimal("15.50"),
                20, // stockActual
                5   // stockMinimo
        );

        // Configuramos un DTO para la creación
        repuestoRequestDTO = new RepuestoRequestDTO(
                "FIL-ACE-001",
                "Filtro de Aceite",
                "Filtro genérico para motor",
                new BigDecimal("15.50"),
                20,
                5
        );
    }

    @Test
    @DisplayName("Obtener todos los repuestos - Éxito")
    void obtenerTodos_Exito() {
        // GIVEN
        when(repuestoRepository.findAll()).thenReturn(List.of(repuestoMock));

        // WHEN
        List<Repuesto> resultado = repuestoService.obtenerTodos();

        // THEN
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        assertEquals("Filtro de Aceite", resultado.get(0).getNombre());
        verify(repuestoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Crear un repuesto - Éxito")
    void crearRepuesto_Exito() {
        // GIVEN
        when(repuestoRepository.save(any(Repuesto.class))).thenReturn(repuestoMock);

        // WHEN
        Repuesto resultado = repuestoService.crearRepuesto(repuestoRequestDTO);

        // THEN
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("FIL-ACE-001", resultado.getCodigoSku());
        verify(repuestoRepository, times(1)).save(any(Repuesto.class));
    }

    @Test
    @DisplayName("Reducir Stock - Éxito")
    void reducirStock_Exito() {
        // GIVEN
        StockReductionDTO dto = new StockReductionDTO(1L, 5); // Queremos usar 5 unidades
        when(repuestoRepository.findById(1L)).thenReturn(Optional.of(repuestoMock));

        // WHEN
        repuestoService.reducirStock(dto);

        // THEN
        // El stock inicial era 20, le quitamos 5, debería quedar en 15.
        assertEquals(15, repuestoMock.getStockActual());
        // Verificamos que se guardó la actualización en la BD
        verify(repuestoRepository, times(1)).save(repuestoMock);
    }

    @Test
    @DisplayName("Reducir Stock - Lanza excepción si el stock es insuficiente (RN-07)")
    void reducirStock_LanzaExcepcion_StockInsuficiente() {
        // GIVEN
        StockReductionDTO dto = new StockReductionDTO(1L, 25); // Queremos usar 25, pero solo hay 20
        when(repuestoRepository.findById(1L)).thenReturn(Optional.of(repuestoMock));

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repuestoService.reducirStock(dto);
        });

        assertTrue(exception.getMessage().contains("Stock insuficiente para el repuesto"));
        // Nos aseguramos de que NUNCA se llame al método save si falla la validación
        verify(repuestoRepository, never()).save(any(Repuesto.class));
    }

    @Test
    @DisplayName("Reducir Stock - Lanza excepción si no se encuentra el repuesto")
    void reducirStock_LanzaExcepcion_NoEncontrado() {
        // GIVEN
        StockReductionDTO dto = new StockReductionDTO(999L, 2);
        when(repuestoRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repuestoService.reducirStock(dto);
        });

        assertEquals("Repuesto no encontrado en el radar", exception.getMessage());
        verify(repuestoRepository, never()).save(any(Repuesto.class));
    }
}