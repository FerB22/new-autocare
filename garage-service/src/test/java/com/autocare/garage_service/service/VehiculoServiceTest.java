package com.autocare.garage_service.service;

import com.autocare.garage_service.client.LoyaltyClient;
import com.autocare.garage_service.dto.ClienteRequestDTO;
import com.autocare.garage_service.dto.VehiculoRequestDTO;
import com.autocare.garage_service.model.Cliente;
import com.autocare.garage_service.model.Vehiculo;
import com.autocare.garage_service.repository.ClienteRepository;
import com.autocare.garage_service.repository.VehiculoRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GarageServiceTest {

    @Mock private ClienteRepository clienteRepository;
    @Mock private VehiculoRepository vehiculoRepository;
    @Mock private LoyaltyClient loyaltyClient;

    @InjectMocks
    private GarageService garageService;

    // ── Datos de prueba reutilizables ──────────────────────────────────────
    private ClienteRequestDTO clienteDTO;
    private VehiculoRequestDTO vehiculoDTO;
    private Cliente clienteExistente;
    private Vehiculo vehiculoExistente;

    @BeforeEach
    void setUp() {
        // Records: los campos van en orden exacto del record
        clienteDTO = new ClienteRequestDTO(
            "12345678-9",       // documentoIdentidad
            "Juan",             // nombre
            "Pérez",            // apellido
            "juan@mail.com",    // email
            "+56912345678"      // telefono
        );

        vehiculoDTO = new VehiculoRequestDTO(
            "AB-CD-12",         // patente
            "Toyota",           // marca
            "Corolla",          // modelo
            2020,               // anio
            "Blanco",           // color
            "VIN12345678901234" // vin (max 17 chars)
        );

        clienteExistente = new Cliente();
        clienteExistente.setId(1L);
        clienteExistente.setDocumentoIdentidad("12345678-9");
        clienteExistente.setNombre("Juan");
        clienteExistente.setApellido("Pérez");
        clienteExistente.setEmail("juan@mail.com");
        clienteExistente.setTelefono("+56912345678");

        vehiculoExistente = new Vehiculo(
            10L, "AB-CD-12", "Toyota", "Corolla", 2020, "Blanco", "VIN12345678901234", clienteExistente
        );
    }

    // =====================================================
    // registrarCliente()
    // =====================================================

    @Test
    @DisplayName("registrarCliente: cliente nuevo se guarda y llama a LoyaltyClient")
    void registrarCliente_NuevoCliente_DeberiaGuardarYLlamarLoyalty() {
        // GIVEN
        when(clienteRepository.findByDocumentoIdentidad("12345678-9")).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteExistente);
        doNothing().when(loyaltyClient).inicializarPerfilLealtad(anyLong());

        // WHEN
        Cliente resultado = garageService.registrarCliente(clienteDTO);

        // THEN
        assertNotNull(resultado);
        assertEquals("Juan", resultado.getNombre());
        verify(clienteRepository).save(any(Cliente.class));
        verify(loyaltyClient).inicializarPerfilLealtad(1L);
    }

    @Test
    @DisplayName("registrarCliente: documento duplicado lanza RuntimeException (RN-01)")
    void registrarCliente_DocumentoDuplicado_DeberiaLanzarExcepcion() {
        // GIVEN
        when(clienteRepository.findByDocumentoIdentidad("12345678-9"))
            .thenReturn(Optional.of(clienteExistente));

        // WHEN & THEN
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> garageService.registrarCliente(clienteDTO));

        assertTrue(ex.getMessage().contains("ya existe"));
        verify(clienteRepository, never()).save(any());
        verify(loyaltyClient, never()).inicializarPerfilLealtad(anyLong());
    }

    @Test
    @DisplayName("registrarCliente: con documento duplicado nunca llama a save")
    void registrarCliente_DocumentoDuplicado_NuncaLlamaSave() {
        // GIVEN
        when(clienteRepository.findByDocumentoIdentidad(anyString()))
            .thenReturn(Optional.of(clienteExistente));

        // WHEN & THEN
        assertThrows(RuntimeException.class, () -> garageService.registrarCliente(clienteDTO));
        verify(clienteRepository, never()).save(any());
    }

    // =====================================================
    // registrarVehiculoEnGarage()
    // =====================================================

    @Test
    @DisplayName("registrarVehiculo: vehículo nuevo se vincula al cliente correcto")
    void registrarVehiculo_ClienteExiste_DeberiaGuardarVehiculo() {
        // GIVEN
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        when(vehiculoRepository.findByPatente("AB-CD-12")).thenReturn(Optional.empty());
        when(vehiculoRepository.save(any(Vehiculo.class))).thenReturn(vehiculoExistente);

        // WHEN
        Vehiculo resultado = garageService.registrarVehiculoEnGarage(1L, vehiculoDTO);

        // THEN
        assertNotNull(resultado);
        assertEquals("AB-CD-12", resultado.getPatente());
        verify(vehiculoRepository).save(any(Vehiculo.class));
    }

    @Test
    @DisplayName("registrarVehiculo: cliente inexistente lanza RuntimeException")
    void registrarVehiculo_ClienteNoExiste_DeberiaLanzarExcepcion() {
        // GIVEN
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> garageService.registrarVehiculoEnGarage(99L, vehiculoDTO));

        assertTrue(ex.getMessage().contains("Cliente no encontrado"));
        verify(vehiculoRepository, never()).save(any());
    }

    @Test
    @DisplayName("registrarVehiculo: patente duplicada lanza RuntimeException")
    void registrarVehiculo_PatenteDuplicada_DeberiaLanzarExcepcion() {
        // GIVEN
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        when(vehiculoRepository.findByPatente("AB-CD-12")).thenReturn(Optional.of(vehiculoExistente));

        // WHEN & THEN
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> garageService.registrarVehiculoEnGarage(1L, vehiculoDTO));

        assertTrue(ex.getMessage().contains("ya está registrado"));
        verify(vehiculoRepository, never()).save(any());
    }

    // =====================================================
    // obtenerPerfilCompleto()
    // =====================================================

    @Test
    @DisplayName("obtenerPerfilCompleto: cliente encontrado retorna el objeto")
    void obtenerPerfilCompleto_ClienteExiste_DeberiaRetornarCliente() {
        // GIVEN
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente));

        // WHEN
        Cliente resultado = garageService.obtenerPerfilCompleto(1L);

        // THEN
        assertEquals(1L, resultado.getId());
        assertEquals("Juan", resultado.getNombre());
    }

    @Test
    @DisplayName("obtenerPerfilCompleto: cliente no encontrado lanza RuntimeException")
    void obtenerPerfilCompleto_ClienteNoExiste_DeberiaLanzarExcepcion() {
        // GIVEN
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> garageService.obtenerPerfilCompleto(99L));

        assertTrue(ex.getMessage().contains("no encontrado"));
    }

    // =====================================================
    // obtenerTodosLosClientes()
    // =====================================================

    @Test
    @DisplayName("obtenerTodosLosClientes: retorna lista de todos los clientes")
    void obtenerTodosLosClientes_DeberiaRetornarLista() {
        // GIVEN
        when(clienteRepository.findAll()).thenReturn(List.of(clienteExistente));

        // WHEN
        List<Cliente> resultado = garageService.obtenerTodosLosClientes();

        // THEN
        assertEquals(1, resultado.size());
        verify(clienteRepository).findAll();
    }

    @Test
    @DisplayName("obtenerTodosLosClientes: sin clientes retorna lista vacía")
    void obtenerTodosLosClientes_SinClientes_DeberiaRetornarListaVacia() {
        // GIVEN
        when(clienteRepository.findAll()).thenReturn(List.of());

        // WHEN
        List<Cliente> resultado = garageService.obtenerTodosLosClientes();

        // THEN
        assertTrue(resultado.isEmpty());
    }

    // =====================================================
    // actualizarCliente()
    // =====================================================

    @Test
    @DisplayName("actualizarCliente: actualiza datos del mismo cliente correctamente")
    void actualizarCliente_ClienteExiste_DeberiaActualizarDatos() {
        // GIVEN — el documento ya pertenece al mismo cliente (ID igual), no es conflicto
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        when(clienteRepository.findByDocumentoIdentidad("12345678-9"))
            .thenReturn(Optional.of(clienteExistente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteExistente);

        // WHEN
        Cliente resultado = garageService.actualizarCliente(1L, clienteDTO);

        // THEN
        assertNotNull(resultado);
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    @DisplayName("actualizarCliente: cliente no encontrado lanza RuntimeException")
    void actualizarCliente_ClienteNoExiste_DeberiaLanzarExcepcion() {
        // GIVEN
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> garageService.actualizarCliente(99L, clienteDTO));

        assertTrue(ex.getMessage().contains("no encontrado"));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("actualizarCliente: documento pertenece a otro cliente lanza RuntimeException")
    void actualizarCliente_DocumentoDeOtroCliente_DeberiaLanzarExcepcion() {
        // GIVEN — "otro cliente" tiene el mismo documento pero distinto ID
        Cliente otroCliente = new Cliente();
        otroCliente.setId(2L);
        otroCliente.setDocumentoIdentidad("12345678-9");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        when(clienteRepository.findByDocumentoIdentidad("12345678-9"))
            .thenReturn(Optional.of(otroCliente));

        // WHEN & THEN
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> garageService.actualizarCliente(1L, clienteDTO));

        assertTrue(ex.getMessage().contains("ya pertenece a otro cliente"));
    }

    // =====================================================
    // eliminarCliente()
    // =====================================================

    @Test
    @DisplayName("eliminarCliente: cliente encontrado es eliminado")
    void eliminarCliente_ClienteExiste_DeberiaEliminar() {
        // GIVEN
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        doNothing().when(clienteRepository).delete(clienteExistente);

        // WHEN
        garageService.eliminarCliente(1L);

        // THEN
        verify(clienteRepository).delete(clienteExistente);
    }

    @Test
    @DisplayName("eliminarCliente: cliente no encontrado lanza RuntimeException")
    void eliminarCliente_ClienteNoExiste_DeberiaLanzarExcepcion() {
        // GIVEN
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(RuntimeException.class, () -> garageService.eliminarCliente(99L));
        verify(clienteRepository, never()).delete(any());
    }

    // =====================================================
    // actualizarVehiculo()
    // =====================================================

    @Test
    @DisplayName("actualizarVehiculo: vehículo existente se actualiza correctamente")
    void actualizarVehiculo_ExisteClienteYVehiculo_DeberiaActualizar() {
        // GIVEN — la patente ya pertenece al mismo vehículo (ID igual), no es conflicto
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        when(vehiculoRepository.findById(10L)).thenReturn(Optional.of(vehiculoExistente));
        when(vehiculoRepository.findByPatente("AB-CD-12")).thenReturn(Optional.of(vehiculoExistente));
        when(vehiculoRepository.save(any(Vehiculo.class))).thenReturn(vehiculoExistente);

        // WHEN
        Vehiculo resultado = garageService.actualizarVehiculo(1L, 10L, vehiculoDTO);

        // THEN
        assertNotNull(resultado);
        verify(vehiculoRepository).save(any(Vehiculo.class));
    }

    @Test
    @DisplayName("actualizarVehiculo: cliente no encontrado lanza RuntimeException")
    void actualizarVehiculo_ClienteNoExiste_DeberiaLanzarExcepcion() {
        // GIVEN
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(RuntimeException.class,
            () -> garageService.actualizarVehiculo(99L, 10L, vehiculoDTO));
        verify(vehiculoRepository, never()).save(any());
    }

    @Test
    @DisplayName("actualizarVehiculo: vehículo no encontrado lanza RuntimeException")
    void actualizarVehiculo_VehiculoNoExiste_DeberiaLanzarExcepcion() {
        // GIVEN
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        when(vehiculoRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> garageService.actualizarVehiculo(1L, 99L, vehiculoDTO));

        assertTrue(ex.getMessage().contains("Vehículo no encontrado"));
    }

    @Test
    @DisplayName("actualizarVehiculo: patente usada por otro vehículo lanza RuntimeException")
    void actualizarVehiculo_PatenteDuplicadaOtroVehiculo_DeberiaLanzarExcepcion() {
        // GIVEN — otro vehículo distinto (ID 20) tiene la misma patente
        Vehiculo otroVehiculo = new Vehiculo(
            20L, "AB-CD-12", "Honda", "Civic", 2019, "Rojo", "VIN999999999999999", null
        );

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        when(vehiculoRepository.findById(10L)).thenReturn(Optional.of(vehiculoExistente));
        when(vehiculoRepository.findByPatente("AB-CD-12")).thenReturn(Optional.of(otroVehiculo));

        // WHEN & THEN
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> garageService.actualizarVehiculo(1L, 10L, vehiculoDTO));

        assertTrue(ex.getMessage().contains("patente ya está registrada"));
    }

    // =====================================================
    // eliminarVehiculo()
    // =====================================================

    @Test
    @DisplayName("eliminarVehiculo: vehículo encontrado es eliminado")
    void eliminarVehiculo_ExisteClienteYVehiculo_DeberiaEliminar() {
        // GIVEN
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        when(vehiculoRepository.findById(10L)).thenReturn(Optional.of(vehiculoExistente));
        doNothing().when(vehiculoRepository).delete(vehiculoExistente);

        // WHEN
        garageService.eliminarVehiculo(1L, 10L);

        // THEN
        verify(vehiculoRepository).delete(vehiculoExistente);
    }

    @Test
    @DisplayName("eliminarVehiculo: cliente no encontrado lanza RuntimeException")
    void eliminarVehiculo_ClienteNoExiste_DeberiaLanzarExcepcion() {
        // GIVEN
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(RuntimeException.class, () -> garageService.eliminarVehiculo(99L, 10L));
        verify(vehiculoRepository, never()).delete(any());
    }

    @Test
    @DisplayName("eliminarVehiculo: vehículo no encontrado lanza RuntimeException")
    void eliminarVehiculo_VehiculoNoExiste_DeberiaLanzarExcepcion() {
        // GIVEN
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        when(vehiculoRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> garageService.eliminarVehiculo(1L, 99L));

        assertTrue(ex.getMessage().contains("Vehículo no encontrado"));
        verify(vehiculoRepository, never()).delete(any());
    }
}