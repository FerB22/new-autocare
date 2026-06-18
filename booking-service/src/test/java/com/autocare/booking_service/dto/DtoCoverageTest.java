package com.autocare.booking_service.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class DtoCoverageTest {

    @Test
    @DisplayName("Forzar cobertura completa de CitaRequestDTO")
    void testCitaRequestDTO() {
        LocalDateTime fecha = LocalDateTime.of(2026, 12, 1, 10, 0);
        CitaRequestDTO dto1 = new CitaRequestDTO(1L, 2L, fecha, "Cambio de aceite");
        CitaRequestDTO dto2 = new CitaRequestDTO(1L, 2L, fecha, "Cambio de aceite");
        CitaRequestDTO dto3 = new CitaRequestDTO(3L, 4L, fecha, "Frenos");

        // Lectura de propiedades de CitaRequestDTO (que sí es un Record)
        assertEquals(1L, dto1.clienteId());
        assertEquals(2L, dto1.vehiculoId());
        assertEquals(fecha, dto1.fechaHora());
        assertEquals("Cambio de aceite", dto1.motivo());

        assertNotNull(dto1.toString());
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
    }

    @Test
    @DisplayName("Forzar cobertura completa de ClienteDTO (Clase Tradicional)")
    void testClienteDTO() {
        ClienteDTO cliente = new ClienteDTO();
        
        // Ejecutamos todos los Setters para dar cobertura
        cliente.setIdCliente(100L);
        cliente.setNombre("Fernando");
        cliente.setApellido("Barra");
        cliente.setEmail("fernando@mail.com");
        cliente.setTelefono("+56912345678");

        // Ejecutamos todos los Getters para verificar y dar cobertura
        assertEquals(100L, cliente.getIdCliente());
        assertEquals("Fernando", cliente.getNombre());
        assertEquals("Barra", cliente.getApellido());
        assertEquals("fernando@mail.com", cliente.getEmail());
        assertEquals("+56912345678", cliente.getTelefono());
        
        // Probamos la propiedad calculada personalizada
        assertEquals("Fernando Barra", cliente.getNombreCompleto());
    }

    @Test
    @DisplayName("Forzar cobertura completa de VehiculoDTO (Clase Tradicional)")
    void testVehiculoDTO() {
        VehiculoDTO vehiculo = new VehiculoDTO();
        
        // Ejecutamos todos los Setters para dar cobertura
        vehiculo.setId(200L);
        vehiculo.setMarca("Toyota");
        vehiculo.setModelo("Corolla");
        vehiculo.setPatente("ABCD123");

        // Ejecutamos todos los Getters para verificar y dar cobertura
        assertEquals(200L, vehiculo.getId());
        assertEquals("Toyota", vehiculo.getMarca());
        assertEquals("Corolla", vehiculo.getModelo());
        assertEquals("ABCD123", vehiculo.getPatente());
        
        // Probamos el método de descripción formateada personalizada
        assertEquals("Toyota Corolla - ABCD123", vehiculo.getDescripcionCompleta());
    }
}