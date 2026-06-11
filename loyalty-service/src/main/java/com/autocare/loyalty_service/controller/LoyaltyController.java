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

@RestController
@RequestMapping("/api/lealtad")
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyService service;

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<PerfilLealtad> consultarPerfil(@PathVariable Long clienteId) {
        return ResponseEntity.ok(service.obtenerPerfil(clienteId));
    }

    @PostMapping("/cliente")
    public ResponseEntity<PerfilLealtad> crearPerfil(@Valid @RequestBody CrearPerfilDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.inicializarPerfil(dto));
    }

    @PostMapping("/cliente/{clienteId}/sumar")
    public ResponseEntity<PerfilLealtad> agregarPuntos(
            @PathVariable Long clienteId,
            @Valid @RequestBody TransaccionPuntosDTO dto) {
        return ResponseEntity.ok(service.sumarPuntos(clienteId, dto));
    }

    @PostMapping("/cliente/{clienteId}/canjear")
    public ResponseEntity<PerfilLealtad> restarPuntos(
            @PathVariable Long clienteId,
            @Valid @RequestBody TransaccionPuntosDTO dto) {
        return ResponseEntity.ok(service.canjearPuntos(clienteId, dto));
    }
}