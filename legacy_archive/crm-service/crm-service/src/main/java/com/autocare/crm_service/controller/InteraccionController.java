package com.autocare.crm_service.controller;

import com.autocare.crm_service.dto.InteraccionRequestDTO;
import com.autocare.crm_service.model.Interaccion;
import com.autocare.crm_service.service.InteraccionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Interacción", description = "Gestión de interacción del taller")
@RestController
@RequestMapping("/interacciones")
public class InteraccionController {

    private final InteraccionService interaccionService;

    public InteraccionController(InteraccionService interaccionService) {
        this.interaccionService = interaccionService;
    }

    @Operation(summary = "Listar todas las interacciones")
    @GetMapping
    public ResponseEntity<List<Interaccion>> listar() {
        return ResponseEntity.ok(interaccionService.listarTodas());
    }

    @Operation(summary = "Obtener por ID")
    @GetMapping("/{id}")
    public ResponseEntity<Object> buscarPorId(@PathVariable String id) {
        Optional<Interaccion> resultado = interaccionService.buscarPorId(id);

        if (resultado.isPresent()) {
            return ResponseEntity.ok(resultado.get());
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Interacción no encontrada con ID: " + id));
    }

    @Operation(summary = "Buscar interacciones por cliente")
    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<Interaccion>> buscarPorCliente(@PathVariable String idCliente) {
        return ResponseEntity.ok(interaccionService.buscarPorCliente(idCliente));
    }

    @Operation(summary = "Buscar interacciones por tipo")
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<Object> buscarPorTipo(@PathVariable Interaccion.TipoInteraccion tipo) {
        return ResponseEntity.ok(interaccionService.buscarPorTipo(tipo));
    }

    @Operation(summary = "Buscar interacciones por seguimiento")
    @GetMapping("/seguimiento/{seguimiento}")
    public ResponseEntity<Object> buscarPorSeguimiento(
            @PathVariable Interaccion.SeguimientoEstado seguimiento) {
        return ResponseEntity.ok(interaccionService.buscarPorSeguimiento(seguimiento));
    }

    @Operation(summary = "Crear nueva interacción")
    @PostMapping
    public ResponseEntity<Object> registrar(@Valid @RequestBody InteraccionRequestDTO dto) {
        try {
            Interaccion interaccion = mapearDtoAEntidad(dto);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(interaccionService.registrar(interaccion));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Cambiar estado de seguimiento")
    @PatchMapping("/{id}/seguimiento")
    public ResponseEntity<Object> cambiarSeguimiento(
            @PathVariable String id,
            @RequestParam Interaccion.SeguimientoEstado estado) {
        try {
            return ResponseEntity.ok(interaccionService.cambiarSeguimiento(id, estado));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Eliminar por ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> eliminar(@PathVariable String id) {
        try {
            interaccionService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    private Interaccion mapearDtoAEntidad(InteraccionRequestDTO dto) {
        Interaccion interaccion = new Interaccion();
        interaccion.setIdCliente(dto.getIdCliente());
        interaccion.setTipo(dto.getTipo());
        interaccion.setDescripcion(dto.getDescripcion());
        return interaccion;
    }
}