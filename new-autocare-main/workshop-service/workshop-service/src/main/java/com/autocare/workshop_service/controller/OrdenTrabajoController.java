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
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Taller", description = "Gestión de órdenes de trabajo: creación, seguimiento y uso de repuestos en reparaciones")
@RestController
@RequestMapping("/api/taller")
@RequiredArgsConstructor
public class OrdenTrabajoController {

    private final OrdenTrabajoService service;

    @Operation(
        summary = "Listar todas las órdenes de trabajo",
        description = "Retorna una lista con todas las órdenes de trabajo registradas en el sistema, sin importar su estado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de órdenes de trabajo obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<List<OrdenTrabajo>> listarOrdenes() {
        return ResponseEntity.ok(service.obtenerTodas());
    }

    @Operation(
        summary = "Crear una nueva orden de trabajo",
        description = "Crea y persiste una nueva orden de trabajo asociada a un vehículo y un mecánico. El estado inicial es RECEPCIONADO y la fecha de creación se asigna automáticamente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Orden de trabajo creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos del request inválidos o error de negocio al crear la orden")
    })
    @PostMapping
    public ResponseEntity<Object> crearOrden(@Valid @RequestBody OrdenTrabajoRequestDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.crearOrden(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
        summary = "Usar un repuesto en una orden de trabajo",
        description = "Registra el uso de un repuesto en la orden indicada y descuenta el stock en inventory-service. Si la orden estaba en estado RECEPCIONADO, cambia automáticamente a EN_PROGRESO."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Repuesto utilizado y orden de trabajo actualizada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Stock insuficiente en inventario o datos del request inválidos"),
        @ApiResponse(responseCode = "404", description = "No existe una orden de trabajo con el ID proporcionado")
    })
    @PostMapping("/{id}/usar-repuesto")
    public ResponseEntity<Object> usarRepuesto(
            @PathVariable Long id,
            @Valid @RequestBody AsignarRepuestoDTO dto) {
        try {
            return ResponseEntity.ok(service.utilizarRepuestoEnOrden(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
}