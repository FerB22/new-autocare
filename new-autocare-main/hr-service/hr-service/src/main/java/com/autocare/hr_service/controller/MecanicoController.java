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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Personal", description = "Gestión del personal mecánico del taller: registro, disponibilidad y especialidades")
@RestController
@RequestMapping("/api/personal")
public class MecanicoController {

    private final MecanicoService mecanicoService;

    public MecanicoController(MecanicoService mecanicoService) {
        this.mecanicoService = mecanicoService;
    }

    @Operation(
        summary = "Listar todos los mecánicos",
        description = "Retorna una lista con todos los mecánicos registrados en el sistema, sin importar su disponibilidad."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de mecánicos obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<List<Mecanico>> listar() {
        return ResponseEntity.ok(mecanicoService.listarTodos());
    }

    @Operation(
        summary = "Obtener mecánico por ID",
        description = "Busca y retorna un mecánico específico según su identificador único. Retorna 404 si no existe."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Mecánico encontrado y retornado exitosamente"),
        @ApiResponse(responseCode = "404", description = "No existe un mecánico con el ID proporcionado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Object> buscarPorId(@PathVariable Long id) {
        Optional<Mecanico> resultado = mecanicoService.buscarPorId(id);
        if (resultado.isPresent()) {
            return ResponseEntity.ok(resultado.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Mecánico no encontrado con ID: " + id));
    }

    @Operation(
        summary = "Listar mecánicos disponibles",
        description = "Retorna únicamente los mecánicos que están disponibles para ser asignados a una orden de trabajo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de mecánicos disponibles obtenida exitosamente")
    })
    @GetMapping("/disponibles")
    public ResponseEntity<List<Mecanico>> listarDisponibles() {
        return ResponseEntity.ok(mecanicoService.buscarDisponibles());
    }

    @Operation(
        summary = "Buscar mecánicos por especialidad",
        description = "Filtra y retorna todos los mecánicos que tengan la especialidad indicada. Valores permitidos: MOTOR, FRENOS, ELECTRICO, SUSPENSION, TRANSMISION, CARROCERIA, GENERAL."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de mecánicos filtrada por especialidad exitosamente"),
        @ApiResponse(responseCode = "400", description = "La especialidad proporcionada no es un valor válido")
    })
    @GetMapping("/especialidad/{especialidad}")
    public ResponseEntity<List<Mecanico>> buscarPorEspecialidad(@PathVariable String especialidad) {
        return ResponseEntity.ok(mecanicoService.buscarPorEspecialidad(especialidad));
    }

    @Operation(
        summary = "Registrar un nuevo mecánico",
        description = "Crea y persiste un nuevo mecánico en el sistema. Valida que el documento de identidad no esté duplicado y que la especialidad sea válida. El mecánico entra disponible por defecto."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Mecánico registrado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Documento de identidad duplicado, especialidad inválida o datos del request incorrectos")
    })
    @PostMapping
    public ResponseEntity<Object> crear(@Valid @RequestBody MecanicoRequestDTO dto) {
        try {
            Mecanico mecanico = mapearDtoAEntidad(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(mecanicoService.guardar(mecanico));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
        summary = "Cambiar disponibilidad de un mecánico",
        description = "Actualiza el estado de disponibilidad de un mecánico. No permite asignar el mismo estado que ya tiene actualmente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Disponibilidad actualizada exitosamente"),
        @ApiResponse(responseCode = "400", description = "El mecánico ya tiene el estado de disponibilidad solicitado"),
        @ApiResponse(responseCode = "404", description = "No existe un mecánico con el ID proporcionado")
    })
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

    @Operation(
        summary = "Actualizar datos de un mecánico",
        description = "Reemplaza los datos editables de un mecánico existente: nombre, apellido, teléfono y especialidad. No modifica el documento de identidad ni la disponibilidad."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Mecánico actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Especialidad inválida o datos del request incorrectos"),
        @ApiResponse(responseCode = "404", description = "No existe un mecánico con el ID proporcionado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Object> actualizar(@PathVariable Long id, @Valid @RequestBody MecanicoRequestDTO dto) {
        try {
            Mecanico mecanico = mapearDtoAEntidad(dto);
            return ResponseEntity.ok(mecanicoService.actualizar(id, mecanico));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
        summary = "Eliminar un mecánico",
        description = "Elimina permanentemente un mecánico del sistema. No se puede eliminar si está asignado a una orden activa (no disponible)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Mecánico eliminado exitosamente, sin contenido en la respuesta"),
        @ApiResponse(responseCode = "400", description = "El mecánico está asignado a una orden activa y no puede eliminarse"),
        @ApiResponse(responseCode = "404", description = "No existe un mecánico con el ID proporcionado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> eliminar(@PathVariable Long id) {
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