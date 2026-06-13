package com.autocare.hr_service.controller;

import com.autocare.hr_service.dto.MecanicoRequestDTO;
import com.autocare.hr_service.model.Mecanico;
import com.autocare.hr_service.service.MecanicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/personal")
@Tag(
    name = "Módulo de Mecánicos",
    description = "Servicios REST para gestionar mecánicos, disponibilidad y búsqueda por especialidad."
)
public class MecanicoController {

    private final MecanicoService mecanicoService;

    public MecanicoController(MecanicoService mecanicoService) {
        this.mecanicoService = mecanicoService;
    }

    @GetMapping
    @Operation(
        summary = "Listar todos los mecánicos",
        description = "Retorna el listado completo de mecánicos registrados en el sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido con éxito.")
    })
    public ResponseEntity<List<Mecanico>> listar() {
        return ResponseEntity.ok(mecanicoService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar mecánico por ID",
        description = "Recupera un mecánico específico usando su identificador numérico."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Mecánico encontrado correctamente."),
        @ApiResponse(responseCode = "404", description = "No existe un mecánico con ese ID.")
    })
    public ResponseEntity<Object> buscarPorId(
            @Parameter(description = "ID del mecánico", required = true)
            @PathVariable Long id) {
        Optional<Mecanico> resultado = mecanicoService.buscarPorId(id);
        if (resultado.isPresent()) {
            return ResponseEntity.ok(resultado.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Mecánico no encontrado con ID: " + id));
    }

    @GetMapping("/disponibles")
    @Operation(
        summary = "Listar mecánicos disponibles",
        description = "Devuelve solo los mecánicos que se encuentran disponibles para ser asignados."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado de mecánicos disponibles obtenido con éxito.")
    })
    public ResponseEntity<List<Mecanico>> listarDisponibles() {
        return ResponseEntity.ok(mecanicoService.buscarDisponibles());
    }

    @GetMapping("/especialidad/{especialidad}")
    @Operation(
        summary = "Buscar mecánicos por especialidad",
        description = "Filtra mecánicos por su área de especialización técnica."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Búsqueda realizada correctamente.")
    })
    public ResponseEntity<List<Mecanico>> buscarPorEspecialidad(
            @Parameter(description = "Especialidad técnica del mecánico", required = true)
            @PathVariable String especialidad) {
        return ResponseEntity.ok(mecanicoService.buscarPorEspecialidad(especialidad));
    }

    @PostMapping
    @Operation(
        summary = "Crear un mecánico",
        description = "Registra un nuevo mecánico validando los datos del DTO antes de persistirlo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Mecánico creado con éxito."),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o error de negocio.")
    })
    public ResponseEntity<Object> crear(@Valid @RequestBody MecanicoRequestDTO dto) {
        try {
            Mecanico mecanico = mapearDtoAEntidad(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(mecanicoService.guardar(mecanico));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/disponibilidad")
    @Operation(
        summary = "Cambiar disponibilidad del mecánico",
        description = "Actualiza el estado de disponibilidad de un mecánico específico."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Disponibilidad actualizada correctamente."),
        @ApiResponse(responseCode = "404", description = "No se encontró el mecánico.")
    })
    public ResponseEntity<Object> cambiarDisponibilidad(
            @Parameter(description = "ID del mecánico", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nuevo estado de disponibilidad", required = true)
            @RequestParam boolean disponible) {
        try {
            return ResponseEntity.ok(mecanicoService.cambiarDisponibilidad(id, disponible));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar mecánico",
        description = "Modifica los datos de un mecánico ya existente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Mecánico actualizado correctamente."),
        @ApiResponse(responseCode = "404", description = "No se encontró el mecánico.")
    })
    public ResponseEntity<Object> actualizar(
            @Parameter(description = "ID del mecánico", required = true)
            @PathVariable Long id,
            @Valid @RequestBody MecanicoRequestDTO dto) {
        try {
            Mecanico mecanico = mapearDtoAEntidad(dto);
            return ResponseEntity.ok(mecanicoService.actualizar(id, mecanico));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar mecánico",
        description = "Elimina un mecánico del sistema si existe."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Mecánico eliminado correctamente."),
        @ApiResponse(responseCode = "404", description = "No se encontró el mecánico.")
    })
    public ResponseEntity<Object> eliminar(
            @Parameter(description = "ID del mecánico", required = true)
            @PathVariable Long id) {
        try {
            mecanicoService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

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