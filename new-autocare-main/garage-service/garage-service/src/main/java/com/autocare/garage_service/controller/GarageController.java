package com.autocare.garage_service.controller;

import com.autocare.garage_service.dto.ClienteRequestDTO;
import com.autocare.garage_service.dto.VehiculoRequestDTO;
import com.autocare.garage_service.model.Cliente;
import com.autocare.garage_service.model.Vehiculo;
import com.autocare.garage_service.service.GarageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Garage", description = "Gestión de clientes y sus vehículos registrados en el taller")
@RestController
@RequestMapping("/api/garage/clientes")
@RequiredArgsConstructor
public class GarageController {

    private final GarageService service;

    @Operation(
        summary = "Listar todos los clientes",
        description = "Retorna una lista con todos los clientes registrados en el sistema del taller."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de clientes obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<List<Cliente>> listarTodos() {
        return ResponseEntity.ok(service.obtenerTodosLosClientes());
    }

    @Operation(
        summary = "Obtener perfil completo de un cliente",
        description = "Busca y retorna el perfil completo de un cliente por su ID, incluyendo sus datos personales."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perfil del cliente obtenido exitosamente"),
        @ApiResponse(responseCode = "404", description = "No existe un cliente con el ID proporcionado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Object> obtenerClientePorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.obtenerPerfilCompleto(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
        summary = "Registrar un nuevo cliente",
        description = "Crea un nuevo cliente en el sistema. Valida que el documento de identidad no esté duplicado e inicializa automáticamente su perfil de lealtad."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cliente registrado exitosamente"),
        @ApiResponse(responseCode = "400", description = "El documento de identidad ya existe o los datos del request son inválidos")
    })
    @PostMapping
    public ResponseEntity<Object> crearCliente(@Valid @RequestBody ClienteRequestDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarCliente(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
        summary = "Agregar un vehículo a un cliente",
        description = "Registra un nuevo vehículo y lo asocia al cliente indicado por su ID. Valida que la patente no esté duplicada en el sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Vehículo registrado y asociado al cliente exitosamente"),
        @ApiResponse(responseCode = "400", description = "La patente ya existe en el sistema o los datos del request son inválidos"),
        @ApiResponse(responseCode = "404", description = "No existe un cliente con el ID proporcionado")
    })
    @PostMapping("/{clienteId}/vehiculos")
    public ResponseEntity<Object> agregarVehiculo(
            @PathVariable Long clienteId,
            @Valid @RequestBody VehiculoRequestDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarVehiculoEnGarage(clienteId, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
}