package com.autocare.loyalty_service.controller;

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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/lealtad")
@RequiredArgsConstructor
@Tag(
    name = "Módulo de Lealtad",
    description = "Servicios REST para consultar y gestionar perfiles de lealtad de clientes, incluyendo acumulación y canje de puntos."
)
public class LoyaltyController {

    private final LoyaltyService service;

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
        EntityModel<PerfilLealtad> recurso = EntityModel.of(perfil);
        recurso.add(linkTo(methodOn(LoyaltyController.class).consultarPerfil(clienteId)).withSelfRel());
        recurso.add(linkTo(methodOn(LoyaltyController.class).agregarPuntos(clienteId, null)).withRel("sumar-puntos"));
        recurso.add(linkTo(methodOn(LoyaltyController.class).restarPuntos(clienteId, null)).withRel("canjear-puntos"));
        return ResponseEntity.ok(recurso);
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
        EntityModel<PerfilLealtad> recurso = EntityModel.of(perfil);
        recurso.add(linkTo(methodOn(LoyaltyController.class).consultarPerfil(perfil.getClienteId())).withSelfRel());
        recurso.add(linkTo(methodOn(LoyaltyController.class).agregarPuntos(perfil.getClienteId(), null)).withRel("sumar-puntos"));
        recurso.add(linkTo(methodOn(LoyaltyController.class).restarPuntos(perfil.getClienteId(), null)).withRel("canjear-puntos"));
        return ResponseEntity.status(HttpStatus.CREATED).body(recurso);
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
        EntityModel<PerfilLealtad> recurso = EntityModel.of(perfil);
        recurso.add(linkTo(methodOn(LoyaltyController.class).consultarPerfil(clienteId)).withSelfRel());
        recurso.add(linkTo(methodOn(LoyaltyController.class).restarPuntos(clienteId, null)).withRel("canjear-puntos"));
        return ResponseEntity.ok(recurso);
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
        EntityModel<PerfilLealtad> recurso = EntityModel.of(perfil);
        recurso.add(linkTo(methodOn(LoyaltyController.class).consultarPerfil(clienteId)).withSelfRel());
        recurso.add(linkTo(methodOn(LoyaltyController.class).agregarPuntos(clienteId, null)).withRel("sumar-puntos"));
        return ResponseEntity.ok(recurso);
    }
}