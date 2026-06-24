package com.autocare.garage_service.controller;

import com.autocare.garage_service.dto.ClienteRequestDTO;
import com.autocare.garage_service.dto.VehiculoRequestDTO;
import com.autocare.garage_service.model.Cliente;
import com.autocare.garage_service.model.Vehiculo;
import com.autocare.garage_service.service.GarageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/garage/clientes")
@RequiredArgsConstructor
@Tag(name = "Módulo Garage (Clientes y Vehículos)", description = "Servicios REST para la gestión del padrón de clientes y sus hojas de vida vehiculares.")
public class GarageController {

    private final GarageService service;

    @GetMapping
    @Operation(
        summary = "Listar todos los clientes registrados",
        description = "Recupera la nómina completa de clientes junto con sus respectivos listados de vehículos asociados desde la base de datos."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de clientes obtenida exitosamente.")
    })
    public ResponseEntity<List<Cliente>> listarTodos() {
        return ResponseEntity.ok(service.obtenerTodosLosClientes());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener el perfil detallado de un cliente",
        description = "Busca un cliente por su ID y devuelve su información personal junto con el historial de sus vehículos."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente encontrado y perfil devuelto con éxito."),
        @ApiResponse(responseCode = "404", description = "El cliente con el ID proporcionado no existe en el sistema.")
    })
    public ResponseEntity<Cliente> obtenerClientePorId(
            @Parameter(description = "Identificador numérico único del cliente (Long)", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPerfilCompleto(id));
    }

    @PostMapping
    @Operation(
        summary = "Registrar un nuevo cliente en el sistema",
        description = "Crea un registro maestro de cliente. El sistema valida mediante reglas de negocio que el correo o RUT no se encuentren duplicados (RN-01)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cliente creado exitosamente en el garage."),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o el cliente ya se encuentra registrado.")
    })
    public ResponseEntity<Cliente> crearCliente(@Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarCliente(dto));
    }

    @PostMapping("/{clienteId}/vehiculos")
    @Operation(
        summary = "Asociar un nuevo vehículo a un cliente",
        description = "Registra un vehículo dentro del garage y establece la relación lógica vinculándolo directamente al ID del cliente propietario."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Vehículo registrado y vinculado correctamente."),
        @ApiResponse(responseCode = "400", description = "Estructura del vehículo inválida (ej. patente mal formateada)."),
        @ApiResponse(responseCode = "404", description = "No se puede asociar el vehículo porque el cliente especificado no existe.")
    })
    public ResponseEntity<Vehiculo> agregarVehiculo(
            @Parameter(description = "ID del cliente dueño del vehículo", required = true)
            @PathVariable Long clienteId,
            @Valid @RequestBody VehiculoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarVehiculoEnGarage(clienteId, dto));
    }

    // ── NUEVOS MÉTODOS ──────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar datos de un cliente",
        description = "Modifica la información personal de un cliente existente identificado por su ID."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente actualizado exitosamente."),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos."),
        @ApiResponse(responseCode = "404", description = "El cliente con el ID proporcionado no existe.")
    })
    public ResponseEntity<Cliente> actualizarCliente(
            @Parameter(description = "ID del cliente a actualizar", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.ok(service.actualizarCliente(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar un cliente del sistema",
        description = "Elimina permanentemente un cliente y todos sus vehículos asociados del garage."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cliente eliminado exitosamente."),
        @ApiResponse(responseCode = "404", description = "El cliente con el ID proporcionado no existe.")
    })
    public ResponseEntity<Void> eliminarCliente(
            @Parameter(description = "ID del cliente a eliminar", required = true)
            @PathVariable Long id) {
        service.eliminarCliente(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{clienteId}/vehiculos/{vehiculoId}")
    @Operation(
        summary = "Actualizar datos de un vehículo",
        description = "Modifica la información de un vehículo específico asociado a un cliente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Vehículo actualizado exitosamente."),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos."),
        @ApiResponse(responseCode = "404", description = "El cliente o vehículo especificado no existe.")
    })
    public ResponseEntity<Vehiculo> actualizarVehiculo(
            @Parameter(description = "ID del cliente dueño del vehículo", required = true)
            @PathVariable Long clienteId,
            @Parameter(description = "ID del vehículo a actualizar", required = true)
            @PathVariable Long vehiculoId,
            @Valid @RequestBody VehiculoRequestDTO dto) {
        return ResponseEntity.ok(service.actualizarVehiculo(clienteId, vehiculoId, dto));
    }

    @DeleteMapping("/{clienteId}/vehiculos/{vehiculoId}")
    @Operation(
        summary = "Eliminar un vehículo del garage",
        description = "Elimina permanentemente un vehículo específico del garage, desvinculándolo del cliente propietario."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Vehículo eliminado exitosamente."),
        @ApiResponse(responseCode = "404", description = "El cliente o vehículo especificado no existe.")
    })
    public ResponseEntity<Void> eliminarVehiculo(
            @Parameter(description = "ID del cliente dueño del vehículo", required = true)
            @PathVariable Long clienteId,
            @Parameter(description = "ID del vehículo a eliminar", required = true)
            @PathVariable Long vehiculoId) {
        service.eliminarVehiculo(clienteId, vehiculoId);
        return ResponseEntity.noContent().build();
    }
}