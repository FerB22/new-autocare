package com.autocare.booking_service.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class DtoCoverageTest {

    // =====================================================
    // CitaRequestDTO — Record Java
    // Los Records autogeneran: equals, hashCode, toString
    // =====================================================

    @Test
    @DisplayName("CitaRequestDTO - Leer todas las propiedades del record")
    void testCitaRequestDTO_Propiedades() {
        // GIVEN
        LocalDateTime fecha = LocalDateTime.of(2026, 12, 1, 10, 0);

        // WHEN
        CitaRequestDTO dto = new CitaRequestDTO(1L, 2L, fecha, "Cambio de aceite");

        // THEN — verifica cada accessor del record
        assertEquals(1L, dto.clienteId());
        assertEquals(2L, dto.vehiculoId());
        assertEquals(fecha, dto.fechaHora());
        assertEquals("Cambio de aceite", dto.motivo());
    }

    @Test
    @DisplayName("CitaRequestDTO - Dos records iguales son equals y tienen mismo hashCode")
    void testCitaRequestDTO_EqualsYHashCode() {
        // GIVEN
        LocalDateTime fecha = LocalDateTime.of(2026, 12, 1, 10, 0);
        CitaRequestDTO dto1 = new CitaRequestDTO(1L, 2L, fecha, "Cambio de aceite");
        CitaRequestDTO dto2 = new CitaRequestDTO(1L, 2L, fecha, "Cambio de aceite");

        // THEN
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    @DisplayName("CitaRequestDTO - Dos records con datos distintos NO son equals")
    void testCitaRequestDTO_NotEquals() {
        // GIVEN
        LocalDateTime fecha = LocalDateTime.of(2026, 12, 1, 10, 0);
        CitaRequestDTO dto1 = new CitaRequestDTO(1L, 2L, fecha, "Cambio de aceite");
        CitaRequestDTO dto3 = new CitaRequestDTO(3L, 4L, fecha, "Frenos");

        // THEN
        assertNotEquals(dto1, dto3);
    }

    @Test
    @DisplayName("CitaRequestDTO - toString no es nulo ni vacío")
    void testCitaRequestDTO_ToString() {
        // GIVEN
        CitaRequestDTO dto = new CitaRequestDTO(1L, 2L, LocalDateTime.now().plusDays(1), "Revisión");

        // THEN
        assertNotNull(dto.toString());
        assertFalse(dto.toString().isBlank());
    }

    @Test
    @DisplayName("CitaRequestDTO - No es igual a null ni a otro tipo de objeto")
    void testCitaRequestDTO_EqualsConNull() {
        // GIVEN
        CitaRequestDTO dto = new CitaRequestDTO(1L, 2L, LocalDateTime.now().plusDays(1), "Revisión");

        // THEN
        assertNotEquals(null, dto);
        assertNotEquals("string", dto);
    }

    // =====================================================
    // ClienteDTO — Clase tradicional con getters/setters
    // =====================================================

    @Test
    @DisplayName("ClienteDTO - Setters y Getters funcionan correctamente")
    void testClienteDTO_GettersSetters() {
        // GIVEN
        ClienteDTO cliente = new ClienteDTO();

        // WHEN — ejecuta todos los setters
        cliente.setIdCliente(100L);
        cliente.setNombre("Fernando");
        cliente.setApellido("Barra");
        cliente.setEmail("fernando@mail.com");
        cliente.setTelefono("+56912345678");

        // THEN — verifica todos los getters
        assertEquals(100L, cliente.getIdCliente());
        assertEquals("Fernando", cliente.getNombre());
        assertEquals("Barra", cliente.getApellido());
        assertEquals("fernando@mail.com", cliente.getEmail());
        assertEquals("+56912345678", cliente.getTelefono());
    }

    @Test
    @DisplayName("ClienteDTO - getNombreCompleto concatena nombre y apellido correctamente")
    void testClienteDTO_NombreCompleto() {
        // GIVEN
        ClienteDTO cliente = new ClienteDTO();
        cliente.setNombre("Fernando");
        cliente.setApellido("Barra");

        // WHEN
        String nombreCompleto = cliente.getNombreCompleto();

        // THEN
        assertEquals("Fernando Barra", nombreCompleto);
    }

    @Test
    @DisplayName("ClienteDTO - Objeto creado no es nulo")
    void testClienteDTO_NoEsNulo() {
        // GIVEN & WHEN
        ClienteDTO cliente = new ClienteDTO();

        // THEN
        assertNotNull(cliente);
    }

    @Test
    @DisplayName("ClienteDTO - getNombreCompleto con nombre de una sola palabra")
    void testClienteDTO_NombreCompletoConUnaSolaPalabra() {
        // GIVEN
        ClienteDTO cliente = new ClienteDTO();
        cliente.setNombre("Ana");
        cliente.setApellido("López");

        // THEN
        assertEquals("Ana López", cliente.getNombreCompleto());
    }

    // =====================================================
    // VehiculoDTO — Clase tradicional con getters/setters
    // =====================================================

    @Test
    @DisplayName("VehiculoDTO - Setters y Getters funcionan correctamente")
    void testVehiculoDTO_GettersSetters() {
        // GIVEN
        VehiculoDTO vehiculo = new VehiculoDTO();

        // WHEN — ejecuta todos los setters
        vehiculo.setId(200L);
        vehiculo.setMarca("Toyota");
        vehiculo.setModelo("Corolla");
        vehiculo.setPatente("ABCD123");

        // THEN — verifica todos los getters
        assertEquals(200L, vehiculo.getId());
        assertEquals("Toyota", vehiculo.getMarca());
        assertEquals("Corolla", vehiculo.getModelo());
        assertEquals("ABCD123", vehiculo.getPatente());
    }

    @Test
    @DisplayName("VehiculoDTO - getDescripcionCompleta formatea correctamente marca, modelo y patente")
    void testVehiculoDTO_DescripcionCompleta() {
        // GIVEN
        VehiculoDTO vehiculo = new VehiculoDTO();
        vehiculo.setMarca("Toyota");
        vehiculo.setModelo("Corolla");
        vehiculo.setPatente("ABCD123");

        // WHEN
        String descripcion = vehiculo.getDescripcionCompleta();

        // THEN
        assertEquals("Toyota Corolla - ABCD123", descripcion);
    }

    @Test
    @DisplayName("VehiculoDTO - Objeto creado no es nulo")
    void testVehiculoDTO_NoEsNulo() {
        // GIVEN & WHEN
        VehiculoDTO vehiculo = new VehiculoDTO();

        // THEN
        assertNotNull(vehiculo);
    }

    @Test
    @DisplayName("VehiculoDTO - getDescripcionCompleta con otra marca y modelo")
    void testVehiculoDTO_DescripcionCompletaOtraMarca() {
        // GIVEN
        VehiculoDTO vehiculo = new VehiculoDTO();
        vehiculo.setMarca("Honda");
        vehiculo.setModelo("Civic");
        vehiculo.setPatente("WXYZ99");

        // THEN
        assertEquals("Honda Civic - WXYZ99", vehiculo.getDescripcionCompleta());
    }
}
