package com.autocare.hr_service.controller;

import com.autocare.hr_service.dto.MecanicoRequestDTO;
import com.autocare.hr_service.model.Mecanico;
import com.autocare.hr_service.service.MecanicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/personal") // Alineado con la ruta del API Gateway
public class MecanicoController {

    private final MecanicoService mecanicoService;

    public MecanicoController(MecanicoService mecanicoService) {
        this.mecanicoService = mecanicoService;
    }

    @GetMapping
    public ResponseEntity<List<Mecanico>> listar() {
        return ResponseEntity.ok(mecanicoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> buscarPorId(@PathVariable Long id) { // Cambiado a Long
        Optional<Mecanico> resultado = mecanicoService.buscarPorId(id);
        if (resultado.isPresent()) {
            return ResponseEntity.ok(resultado.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Mecánico no encontrado con ID: " + id));
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<Mecanico>> listarDisponibles() {
        return ResponseEntity.ok(mecanicoService.buscarDisponibles());
    }

    @GetMapping("/especialidad/{especialidad}")
    public ResponseEntity<List<Mecanico>> buscarPorEspecialidad(@PathVariable String especialidad) {
        return ResponseEntity.ok(mecanicoService.buscarPorEspecialidad(especialidad));
    }

    @PostMapping
    public ResponseEntity<Object> crear(@Valid @RequestBody MecanicoRequestDTO dto) {
        try {
            Mecanico mecanico = mapearDtoAEntidad(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(mecanicoService.guardar(mecanico));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/disponibilidad")
    public ResponseEntity<Object> cambiarDisponibilidad(
            @PathVariable Long id, 
            @RequestParam boolean disponible) {
        try {
            return ResponseEntity.ok(mecanicoService.cambiarDisponibilidad(id, disponible));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> actualizar(@PathVariable Long id, @Valid @RequestBody MecanicoRequestDTO dto) {
        try {
            Mecanico mecanico = mapearDtoAEntidad(dto);
            return ResponseEntity.ok(mecanicoService.actualizar(id, mecanico));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> eliminar(@PathVariable Long id) {
        try {
            mecanicoService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // Extrae los datos usando los métodos inmutables de Java 21 Records
    private Mecanico mapearDtoAEntidad(MecanicoRequestDTO dto) {
        Mecanico mecanico = new Mecanico();
        mecanico.setDocumentoIdentidad(dto.documentoIdentidad());
        mecanico.setNombre(dto.nombre());
        mecanico.setApellido(dto.apellido());
        mecanico.setEspecialidad(dto.especialidad());
        mecanico.setTelefono(dto.telefono());
        return mecanico;
    }
}