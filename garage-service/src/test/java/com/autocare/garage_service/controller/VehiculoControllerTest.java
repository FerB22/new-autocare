package com.autocare.garage_service.controller;

import com.autocare.garage_service.dto.ClienteRequestDTO;
import com.autocare.garage_service.dto.VehiculoRequestDTO;
import com.autocare.garage_service.model.Cliente;
import com.autocare.garage_service.model.Vehiculo;
import com.autocare.garage_service.service.GarageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GarageController.class)
class GarageControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private GarageService garageService;

    // ── Datos de prueba reutilizables ──────────────────────────────────────
    private ClienteRequestDTO clienteDTO;
    private VehiculoRequestDTO vehiculoDTO;
    private Cliente clienteExistente;
    private Vehiculo vehiculoExistente;

    private static final String BASE_URL   = "/api/garage/clientes";
    private static final String VEHICULO_URL = "/api/garage/clientes/{clienteId}/vehiculos";

    @BeforeEach
    void setUp() {
        clienteDTO = new ClienteRequestDTO(
            "12345678-9", "Juan", "Pérez", "juan@mail.com", "+56912345678"
        );

        vehiculoDTO = new VehiculoRequestDTO(
            "AB-CD-12", "Toyota", "Corolla", 2020, "Blanco", "VIN12345678901234"
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
    // GET /api/garage/clientes  →  listarTodos()
    // =====================================================

    @Test
    @DisplayName("GET /clientes: retorna lista con 200 OK")
    void listarTodos_DeberiaRetornar200() throws Exception {
        // GIVEN
        when(garageService.obtenerTodosLosClientes()).thenReturn(List.of(clienteExistente));

        // WHEN & THEN
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nombre").value("Juan"));
    }

    @Test
    @DisplayName("GET /clientes: lista vacía retorna 200 con array vacío")
    void listarTodos_SinClientes_DeberiaRetornarListaVacia() throws Exception {
        // GIVEN
        when(garageService.obtenerTodosLosClientes()).thenReturn(List.of());

        // WHEN & THEN
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    // =====================================================
    // GET /api/garage/clientes/{id}  →  obtenerClientePorId()
    // =====================================================

    @Test
    @DisplayName("GET /clientes/{id}: cliente existente retorna 200 con datos")
    void obtenerClientePorId_ClienteExiste_DeberiaRetornar200() throws Exception {
        // GIVEN
        when(garageService.obtenerPerfilCompleto(1L)).thenReturn(clienteExistente);

        // WHEN & THEN
        mockMvc.perform(get(BASE_URL + "/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.nombre").value("Juan"))
            .andExpect(jsonPath("$.email").value("juan@mail.com"));
    }

    @Test
    @DisplayName("GET /clientes/{id}: cliente no encontrado retorna 500 (RuntimeException sin manejo)")
    void obtenerClientePorId_ClienteNoExiste_DeberiaRetornarError() throws Exception {
        // GIVEN
        when(garageService.obtenerPerfilCompleto(99L))
            .thenThrow(new RuntimeException("Perfil de cliente no encontrado"));

        // WHEN & THEN
        mockMvc.perform(get(BASE_URL + "/99"))
            .andExpect(status().isBadRequest());
    }

    // =====================================================
    // POST /api/garage/clientes  →  crearCliente()
    // =====================================================

    @Test
    @DisplayName("POST /clientes: cliente válido retorna 201 CREATED")
    void crearCliente_DatosValidos_DeberiaRetornar201() throws Exception {
        // GIVEN
        when(garageService.registrarCliente(any(ClienteRequestDTO.class)))
            .thenReturn(clienteExistente);

        // WHEN & THEN
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    @DisplayName("POST /clientes: body vacío retorna 400 BAD REQUEST por validación")
    void crearCliente_BodyVacio_DeberiaRetornar400() throws Exception {
        // WHEN & THEN — sin body, @Valid rechaza la petición antes de llegar al service
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /clientes: documento duplicado lanza RuntimeException")
    void crearCliente_DocumentoDuplicado_DeberiaRetornarError() throws Exception {
        // GIVEN
        when(garageService.registrarCliente(any(ClienteRequestDTO.class)))
            .thenThrow(new RuntimeException("El cliente con este documento ya existe"));

        // WHEN & THEN
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteDTO)))
            .andExpect(status().isBadRequest());
    }

    // =====================================================
    // POST /api/garage/clientes/{clienteId}/vehiculos  →  agregarVehiculo()
    // =====================================================

    @Test
    @DisplayName("POST /clientes/{id}/vehiculos: vehículo válido retorna 201 CREATED")
    void agregarVehiculo_DatosValidos_DeberiaRetornar201() throws Exception {
        // GIVEN
        when(garageService.registrarVehiculoEnGarage(eq(1L), any(VehiculoRequestDTO.class)))
            .thenReturn(vehiculoExistente);

        // WHEN & THEN
        mockMvc.perform(post(VEHICULO_URL, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vehiculoDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.patente").value("AB-CD-12"));
    }

    @Test
    @DisplayName("POST /clientes/{id}/vehiculos: cliente no existe retorna error")
    void agregarVehiculo_ClienteNoExiste_DeberiaRetornarError() throws Exception {
        // GIVEN
        when(garageService.registrarVehiculoEnGarage(eq(99L), any(VehiculoRequestDTO.class)))
            .thenThrow(new RuntimeException("Cliente no encontrado en el sistema"));

        // WHEN & THEN
        mockMvc.perform(post(VEHICULO_URL, 99L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vehiculoDTO)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /clientes/{id}/vehiculos: patente duplicada retorna error")
    void agregarVehiculo_PatenteDuplicada_DeberiaRetornarError() throws Exception {
        // GIVEN
        when(garageService.registrarVehiculoEnGarage(anyLong(), any(VehiculoRequestDTO.class)))
            .thenThrow(new RuntimeException("Este vehículo ya está registrado en la red"));

        // WHEN & THEN
        mockMvc.perform(post(VEHICULO_URL, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vehiculoDTO)))
            .andExpect(status().isBadRequest());
    }

    // =====================================================
    // PUT /api/garage/clientes/{id}  →  actualizarCliente()
    // =====================================================

    @Test
    @DisplayName("PUT /clientes/{id}: datos válidos retorna 200 OK con cliente actualizado")
    void actualizarCliente_DatosValidos_DeberiaRetornar200() throws Exception {
        // GIVEN
        when(garageService.actualizarCliente(eq(1L), any(ClienteRequestDTO.class)))
            .thenReturn(clienteExistente);

        // WHEN & THEN
        mockMvc.perform(put(BASE_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    @DisplayName("PUT /clientes/{id}: cliente no encontrado retorna error")
    void actualizarCliente_ClienteNoExiste_DeberiaRetornarError() throws Exception {
        // GIVEN
        when(garageService.actualizarCliente(eq(99L), any(ClienteRequestDTO.class)))
            .thenThrow(new RuntimeException("Cliente no encontrado en el sistema"));

        // WHEN & THEN
        mockMvc.perform(put(BASE_URL + "/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteDTO)))
            .andExpect(status().isBadRequest());
    }

    // =====================================================
    // DELETE /api/garage/clientes/{id}  →  eliminarCliente()
    // =====================================================

    @Test
    @DisplayName("DELETE /clientes/{id}: cliente existente retorna 204 NO CONTENT")
    void eliminarCliente_ClienteExiste_DeberiaRetornar204() throws Exception {
        // GIVEN
        doNothing().when(garageService).eliminarCliente(1L);

        // WHEN & THEN
        mockMvc.perform(delete(BASE_URL + "/1"))
            .andExpect(status().isNoContent());

        verify(garageService).eliminarCliente(1L);
    }

    @Test
    @DisplayName("DELETE /clientes/{id}: cliente no encontrado retorna error")
    void eliminarCliente_ClienteNoExiste_DeberiaRetornarError() throws Exception {
        // GIVEN
        doThrow(new RuntimeException("Cliente no encontrado en el sistema"))
            .when(garageService).eliminarCliente(99L);

        // WHEN & THEN
        mockMvc.perform(delete(BASE_URL + "/99"))
            .andExpect(status().isBadRequest());
    }

    // =====================================================
    // PUT /api/garage/clientes/{clienteId}/vehiculos/{vehiculoId}
    // =====================================================

    @Test
    @DisplayName("PUT /clientes/{cId}/vehiculos/{vId}: datos válidos retorna 200 OK")
    void actualizarVehiculo_DatosValidos_DeberiaRetornar200() throws Exception {
        // GIVEN
        when(garageService.actualizarVehiculo(eq(1L), eq(10L), any(VehiculoRequestDTO.class)))
            .thenReturn(vehiculoExistente);

        // WHEN & THEN
        mockMvc.perform(put(VEHICULO_URL + "/{vehiculoId}", 1L, 10L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vehiculoDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.patente").value("AB-CD-12"))
            .andExpect(jsonPath("$.marca").value("Toyota"));
    }

    @Test
    @DisplayName("PUT /clientes/{cId}/vehiculos/{vId}: vehículo no encontrado retorna error")
    void actualizarVehiculo_VehiculoNoExiste_DeberiaRetornarError() throws Exception {
        // GIVEN
        when(garageService.actualizarVehiculo(eq(1L), eq(99L), any(VehiculoRequestDTO.class)))
            .thenThrow(new RuntimeException("Vehículo no encontrado en el garage"));

        // WHEN & THEN
        mockMvc.perform(put(VEHICULO_URL + "/{vehiculoId}", 1L, 99L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vehiculoDTO)))
            .andExpect(status().is5xxServerError());
    }

    // =====================================================
    // DELETE /api/garage/clientes/{clienteId}/vehiculos/{vehiculoId}
    // =====================================================

    @Test
    @DisplayName("DELETE /clientes/{cId}/vehiculos/{vId}: existente retorna 204 NO CONTENT")
    void eliminarVehiculo_Existente_DeberiaRetornar204() throws Exception {
        // GIVEN
        doNothing().when(garageService).eliminarVehiculo(1L, 10L);

        // WHEN & THEN
        mockMvc.perform(delete(VEHICULO_URL + "/{vehiculoId}", 1L, 10L))
            .andExpect(status().isNoContent());

        verify(garageService).eliminarVehiculo(1L, 10L);
    }

    @Test
    @DisplayName("DELETE /clientes/{cId}/vehiculos/{vId}: vehículo no encontrado retorna error")
    void eliminarVehiculo_VehiculoNoExiste_DeberiaRetornarError() throws Exception {
        // GIVEN
        doThrow(new RuntimeException("Vehículo no encontrado en el garage"))
            .when(garageService).eliminarVehiculo(1L, 99L);

        // WHEN & THEN
        mockMvc.perform(delete(VEHICULO_URL + "/{vehiculoId}", 1L, 99L))
            .andExpect(status().is5xxServerError());
    }
}
