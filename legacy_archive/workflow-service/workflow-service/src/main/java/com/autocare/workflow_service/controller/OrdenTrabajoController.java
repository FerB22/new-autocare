package com.autocare.workflow_service.controller;

import com.autocare.workflow_service.dto.OrdenTrabajoRequestDto;
import com.autocare.workflow_service.model.OrdenTrabajo;
import com.autocare.workflow_service.service.OrdenTrabajoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST principal para el microservicio de Flujo de Trabajo (workflow-service).
 */
@Validated
@Tag(name = "Órdenes de Trabajo", description = "Gestión del flujo de trabajo del taller")
@RestController
@RequestMapping("/ordenes")
public class OrdenTrabajoController {

    private final OrdenTrabajoService ordenService;

    public OrdenTrabajoController(OrdenTrabajoService ordenService) {
        this.ordenService = ordenService;
    }

    @Operation(summary = "Listar todas las órdenes de trabajo")
    @GetMapping
    public ResponseEntity<List<OrdenTrabajo>> listar() {
        return ResponseEntity.ok(ordenService.listarTodas());
    }

    @Operation(summary = "Buscar orden de trabajo por ID")
    @GetMapping("/{id}")
    public ResponseEntity<Object> buscarPorId(@PathVariable String id) {
        Optional<OrdenTrabajo> resultado = ordenService.buscarPorId(id);

        if (resultado.isPresent()) {
            return ResponseEntity.ok(resultado.get());
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Orden no encontrada con ID: " + id));
    }

    @Operation(summary = "Buscar órdenes por vehículo")
    @GetMapping("/vehiculo/{idVehiculo}")
    public ResponseEntity<List<OrdenTrabajo>> buscarPorVehiculo(@PathVariable String idVehiculo) {
        return ResponseEntity.ok(ordenService.buscarPorVehiculo(idVehiculo));
    }

    @Operation(summary = "Buscar órdenes por estado")
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<OrdenTrabajo>> buscarPorEstado(@PathVariable OrdenTrabajo.EstadoOrden estado) {
        return ResponseEntity.ok(ordenService.buscarPorEstado(estado));
    }

    @Operation(summary = "Crear nueva orden de trabajo")
    @PostMapping
    public ResponseEntity<Object> crear(@Valid @RequestBody OrdenTrabajoRequestDto ordenDto) {
        try {
            OrdenTrabajo orden = mapearDtoAEntidad(ordenDto);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ordenService.crear(orden));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Cambiar estado de una orden de trabajo")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Object> cambiarEstado(@PathVariable String id,
                                                @RequestParam OrdenTrabajo.EstadoOrden estado) {
        try {
            return ResponseEntity.ok(ordenService.cambiarEstado(id, estado));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Eliminar orden de trabajo por ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> eliminar(@PathVariable String id) {
        try {
            ordenService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    private OrdenTrabajo mapearDtoAEntidad(OrdenTrabajoRequestDto dto) {
        OrdenTrabajo orden = new OrdenTrabajo();
        orden.setIdVehiculo(dto.getIdVehiculo());
        orden.setPrioridad(dto.getPrioridad());
        return orden;
    }
}