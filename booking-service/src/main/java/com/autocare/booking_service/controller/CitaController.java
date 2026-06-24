package com.autocare.booking_service.controller;

import com.autocare.booking_service.assembler.CitaModelAssembler;
import com.autocare.booking_service.dto.CitaRequestDTO;
import com.autocare.booking_service.model.Cita;
import com.autocare.booking_service.service.CitaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservas/citas")
@RequiredArgsConstructor
@Tag(
    name = "Módulo de Citas (Agendamiento)",
    description = "Servicios REST con soporte HATEOAS para la reserva de ventanas horarias, control de capacidad física del taller y gestión de estados de citas."
)
public class CitaController {

    private final CitaService citaService;
    private final CitaModelAssembler assembler;

    @PostMapping
    @Operation(
        summary = "Agendar una nueva cita vehicular",
        description = "Registra una nueva reserva horaria validando entidades, evitando choques de horario y controlando el límite máximo diario de capacidad del taller."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cita agendada con éxito. El recurso retornado incluye enlaces HATEOAS."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida, choque horario o capacidad máxima diaria alcanzada.")
    })
    public ResponseEntity<EntityModel<Cita>> crearCita(
            @Valid @RequestBody CitaRequestDTO citaDTO) {

        Cita nuevaCita = citaService.agendarCita(citaDTO);
        EntityModel<Cita> recurso = assembler.toModel(nuevaCita);

        return ResponseEntity.status(HttpStatus.CREATED).body(recurso);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener una cita por su ID",
        description = "Recupera una cita específica mediante su identificador único e incorpora enlaces HATEOAS según el estado actual del recurso."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cita encontrada y retornada correctamente."),
        @ApiResponse(responseCode = "404", description = "No existe una cita con el identificador proporcionado.")
    })
    public ResponseEntity<EntityModel<Cita>> obtenerCitaPorId(
            @Parameter(description = "Identificador único de la cita", required = true)
            @PathVariable Long id) {

        Cita cita = citaService.obtenerPorId(id);
        return ResponseEntity.ok(assembler.toModel(cita));
    }

    @GetMapping
    @Operation(
        summary = "Listar todas las citas",
        description = "Obtiene la colección completa de citas registradas, retornando cada recurso con su representación HATEOAS."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado de citas recuperado correctamente.")
    })
    public ResponseEntity<CollectionModel<EntityModel<Cita>>> obtenerTodas() {

        List<Cita> citas = citaService.obtenerTodas();
        CollectionModel<EntityModel<Cita>> modeloColeccion = assembler.toCollectionModel(citas);

        return ResponseEntity.ok(modeloColeccion);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar o reprogramar una cita",
        description = "Permite modificar los datos de una cita existente, validando restricciones temporales, reglas de negocio y cambios de estado no permitidos."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cita actualizada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o incumplimiento de reglas de negocio."),
        @ApiResponse(responseCode = "404", description = "No se encontró la cita a actualizar.")
    })
    public ResponseEntity<EntityModel<Cita>> actualizarCita(
            @Parameter(description = "ID de la cita que se desea actualizar", required = true)
            @PathVariable Long id,
            @Valid @RequestBody CitaRequestDTO citaDTO) {

        Cita citaActualizada = citaService.actualizarCita(id, citaDTO);
        return ResponseEntity.ok(assembler.toModel(citaActualizada));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Cancelar o eliminar una cita",
        description = "Elimina una cita registrada, liberando la ventana horaria y actualizando la disponibilidad operativa del taller."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cita eliminada correctamente."),
        @ApiResponse(responseCode = "400", description = "La cita no puede eliminarse debido a restricciones operacionales."),
        @ApiResponse(responseCode = "404", description = "No se encontró la cita a eliminar.")
    })
    public ResponseEntity<Void> eliminarCita(
            @Parameter(description = "ID de la cita que se desea eliminar", required = true)
            @PathVariable Long id) {

        citaService.eliminarCita(id);
        return ResponseEntity.noContent().build();
    }
}