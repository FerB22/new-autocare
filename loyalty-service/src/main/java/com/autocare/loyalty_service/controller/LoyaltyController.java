package com.autocare.loyalty_service.controller;

import com.autocare.loyalty_service.assembler.PerfilLealtadModelAssembler;
import com.autocare.loyalty_service.dto.CrearPerfilDTO;
import com.autocare.loyalty_service.dto.TransaccionPuntosDTO;
import com.autocare.loyalty_service.model.PerfilLealtad;
import com.autocare.loyalty_service.service.LoyaltyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lealtad")
@RequiredArgsConstructor
@Tag(
    name = "Módulo de Lealtad",
    description = "Servicios REST para consultar y gestionar perfiles de lealtad de clientes, incluyendo acumulación y canje de puntos."
)
public class LoyaltyController {

    private final LoyaltyService service;
    private final PerfilLealtadModelAssembler assembler;

    @GetMapping("/cliente/{clienteId}")
    @Operation(
        summary = "Consultar perfil de lealtad por cliente",
        description = "Obtiene el perfil de lealtad asociado a un cliente específico."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perfil encontrado correctamente."),
        @ApiResponse(responseCode = "404", description = "No existe un perfil para el cliente indicado.")
    })
    public ResponseEntity<EntityModel<PerfilLealtad>> consultarPerfil(
            @Parameter(description = "ID del cliente", required = true)
            @PathVariable Long clienteId) {
        PerfilLealtad perfil = service.obtenerPerfil(clienteId);
        return ResponseEntity.ok(assembler.toModel(perfil));
    }

    @PostMapping("/cliente")
    @Operation(
        summary = "Crear perfil de lealtad",
        description = "Inicializa un nuevo perfil de lealtad para un cliente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Perfil creado con éxito."),
        @ApiResponse(responseCode = "400", description = "La solicitud tiene datos inválidos.")
    })
    public ResponseEntity<EntityModel<PerfilLealtad>> crearPerfil(@Valid @RequestBody CrearPerfilDTO dto) {
        PerfilLealtad perfil = service.inicializarPerfil(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(perfil));
    }

    @PostMapping("/cliente/{clienteId}/sumar")
    @Operation(
        summary = "Sumar puntos al perfil",
        description = "Registra una transacción positiva de puntos para el cliente indicado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Puntos agregados correctamente."),
        @ApiResponse(responseCode = "400", description = "La transacción no cumple las reglas de negocio."),
        @ApiResponse(responseCode = "404", description = "No se encontró el perfil del cliente.")
    })
    public ResponseEntity<EntityModel<PerfilLealtad>> agregarPuntos(
            @Parameter(description = "ID del cliente", required = true)
            @PathVariable Long clienteId,
            @Valid @RequestBody TransaccionPuntosDTO dto) {
        PerfilLealtad perfil = service.sumarPuntos(clienteId, dto);
        return ResponseEntity.ok(assembler.toModel(perfil));
    }

    @PostMapping("/cliente/{clienteId}/canjear")
    @Operation(
        summary = "Canjear puntos del perfil",
        description = "Registra una transacción negativa de puntos para el cliente indicado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Puntos canjeados correctamente."),
        @ApiResponse(responseCode = "400", description = "El canje no es válido por falta de puntos o datos incorrectos."),
        @ApiResponse(responseCode = "404", description = "No se encontró el perfil del cliente.")
    })
    public ResponseEntity<EntityModel<PerfilLealtad>> restarPuntos(
            @Parameter(description = "ID del cliente", required = true)
            @PathVariable Long clienteId,
            @Valid @RequestBody TransaccionPuntosDTO dto) {
        PerfilLealtad perfil = service.canjearPuntos(clienteId, dto);
        return ResponseEntity.ok(assembler.toModel(perfil));
    }
}