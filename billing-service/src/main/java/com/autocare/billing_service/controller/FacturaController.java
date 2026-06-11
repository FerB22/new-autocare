package com.autocare.billing_service.controller;

import com.autocare.billing_service.dto.FacturaRequestDTO;
import com.autocare.billing_service.model.Factura;
import com.autocare.billing_service.service.FacturaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Facturacion", description = "Gestión de cobros del taller")
@RestController
@RequestMapping("/api/facturacion")
@RequiredArgsConstructor // Inyecta el FacturaService automáticamente
public class FacturaController {

    private final FacturaService facturaService;

    @Operation(summary = "Listar todas las facturas")
    @GetMapping
    public ResponseEntity<List<Factura>> listar() {
        return ResponseEntity.ok(facturaService.listarTodas());
    }

    @Operation(summary = "Obtener factura por ID")
    @GetMapping("/{id}")
    public ResponseEntity<Object> buscarPorId(@PathVariable Long id) { // Cambiado a Long
        Optional<Factura> resultado = facturaService.buscarPorId(id);
        if (resultado.isPresent()) {
            return ResponseEntity.ok(resultado.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", "Factura no encontrada con ID: " + id));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<Object> buscarPorEstado(@PathVariable Factura.EstadoPago estado) {
        return ResponseEntity.ok(facturaService.buscarPorEstado(estado));
    }

    @Operation(summary = "Generar una nueva factura")
    @PostMapping
    public ResponseEntity<Object> generar(@Valid @RequestBody FacturaRequestDTO facturaRequest) {
        try {
            // Toda la lógica de transformación y cálculo se delegó al Service
            return ResponseEntity.status(HttpStatus.CREATED).body(facturaService.generar(facturaRequest));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Procesar el pago de una factura")
    @PatchMapping("/{id}/pagar")
    public ResponseEntity<Object> pagarFactura(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(facturaService.pagarFactura(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Anular una factura")
    @PatchMapping("/{id}/anular")
    public ResponseEntity<Object> anularFactura(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(facturaService.anularFactura(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Eliminar factura")
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> eliminar(@PathVariable Long id) {
        try {
            facturaService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
}