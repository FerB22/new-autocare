package com.autocare.workshop_service.controller;

import com.autocare.workshop_service.dto.AsignarRepuestoDTO;
import com.autocare.workshop_service.dto.OrdenTrabajoRequestDTO;
import com.autocare.workshop_service.model.OrdenTrabajo;
import com.autocare.workshop_service.service.OrdenTrabajoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/taller")
@RequiredArgsConstructor
public class OrdenTrabajoController {

    private final OrdenTrabajoService service;

    @GetMapping
    public ResponseEntity<List<OrdenTrabajo>> listarOrdenes() {
        return ResponseEntity.ok(service.obtenerTodas());
    }

    @PostMapping
    public ResponseEntity<OrdenTrabajo> crearOrden(@Valid @RequestBody OrdenTrabajoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearOrden(dto));
    }

    @PostMapping("/{id}/usar-repuesto")
    public ResponseEntity<OrdenTrabajo> usarRepuesto(
            @PathVariable Long id,
            @Valid @RequestBody AsignarRepuestoDTO dto) {
        OrdenTrabajo ordenActualizada = service.utilizarRepuestoEnOrden(id, dto);
        return ResponseEntity.ok(ordenActualizada);
    }
}