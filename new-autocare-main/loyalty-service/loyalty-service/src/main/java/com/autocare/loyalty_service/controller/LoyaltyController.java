package com.autocare.loyalty_service.controller;

import com.autocare.loyalty_service.dto.CrearPerfilDTO;
import com.autocare.loyalty_service.dto.TransaccionPuntosDTO;
import com.autocare.loyalty_service.model.PerfilLealtad;
import com.autocare.loyalty_service.service.LoyaltyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Lealtad", description = "Gestión de perfiles de lealtad, acumulación y canje de puntos por cliente")
@RestController
@RequestMapping("/api/lealtad")
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyService service;

    @Operation(
        summary = "Consultar perfil de lealtad de un cliente",
        description = "Retorna el perfil de lealtad de un cliente, incluyendo sus puntos acumulados, nivel actual (BRONCE, PLATA, ORO, VIP) y fecha de última actualización."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perfil de lealtad obtenido exitosamente"),
        @ApiResponse(responseCode = "404", description = "No existe un perfil de lealtad para el cliente con el ID proporcionado")
    })
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<Object> consultarPerfil(@PathVariable Long clienteId) {
        try {
            return ResponseEntity.ok(service.obtenerPerfil(clienteId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
        summary = "Inicializar perfil de lealtad",
        description = "Crea un nuevo perfil de lealtad para un cliente. El perfil inicia con 0 puntos y nivel BRONCE. Falla si el cliente ya tiene un perfil activo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Perfil de lealtad inicializado exitosamente"),
        @ApiResponse(responseCode = "400", description = "El cliente ya tiene un perfil de lealtad registrado")
    })
    @PostMapping("/cliente")
    public ResponseEntity<Object> crearPerfil(@Valid @RequestBody CrearPerfilDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.inicializarPerfil(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
        summary = "Sumar puntos a un cliente",
        description = "Agrega puntos al perfil de lealtad del cliente y recalcula automáticamente su nivel según los umbrales: 500 → PLATA, 2000 → ORO, 5000 → VIP."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Puntos sumados y nivel actualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "No existe un perfil de lealtad para el cliente con el ID proporcionado")
    })
    @PostMapping("/cliente/{clienteId}/sumar")
    public ResponseEntity<Object> agregarPuntos(
            @PathVariable Long clienteId,
            @Valid @RequestBody TransaccionPuntosDTO dto) {
        try {
            return ResponseEntity.ok(service.sumarPuntos(clienteId, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
        summary = "Canjear puntos de un cliente",
        description = "Descuenta puntos del perfil de lealtad del cliente. Falla si los puntos disponibles son menores a la cantidad a canjear. Recalcula el nivel automáticamente tras el descuento."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Puntos canjeados y nivel actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Puntos insuficientes para realizar el canje"),
        @ApiResponse(responseCode = "404", description = "No existe un perfil de lealtad para el cliente con el ID proporcionado")
    })
    @PostMapping("/cliente/{clienteId}/canjear")
    public ResponseEntity<Object> restarPuntos(
            @PathVariable Long clienteId,
            @Valid @RequestBody TransaccionPuntosDTO dto) {
        try {
            return ResponseEntity.ok(service.canjearPuntos(clienteId, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
}