package com.autocare.booking_service.controller;

import com.autocare.booking_service.assembler.CitaModelAssembler;
import com.autocare.booking_service.model.Cita;
import com.autocare.booking_service.dto.CitaRequestDTO;
import com.autocare.booking_service.service.CitaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservas/citas")
@RequiredArgsConstructor
@Tag(name = "Módulo de Citas (Agendamiento)", description = "Servicios REST con soporte HATEOAS para la reserva de ventanas horarias, control de capacidad física del taller y estados de citas.")
public class CitaController {

    private final CitaService citaService;
    private final CitaModelAssembler assembler;

    @PostMapping
    @Operation(
        summary = "Agendar una nueva cita vehicular", 
        description = "Registra una reserva horaria interactuando con los servicios maestros para validar entidades (RN-01), verificar choques horarios (RN-03) y el límite máximo de 20 citas diarias (RN-04)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cita agendada con éxito. El payload de retorno incluye los hiperenlaces HATEOAS relacionales."),
        @ApiResponse(responseCode = "400", description = "Fallo de validación: Choque temporal o el taller alcanzó su capacidad máxima de saturación.")
    })
    public ResponseEntity<EntityModel<Cita>> crearCita(@Valid @RequestBody CitaRequestDTO citaDTO) {
        Cita nuevaCita = citaService.agendarCita(citaDTO);
        // Delegamos al assembler la creación del EntityModel con sus links correspondientes
        EntityModel<Cita> recurso = assembler.toModel(nuevaCita);
        return new ResponseEntity<>(recurso, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener una cita por su ID", 
        description = "Recupera una reserva de atención mecánica específica mediante su clave primaria e inyecta enlaces de navegación de estado dinámicos."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reserva localizada y devuelta junto con sus metadatos navegables."),
        @ApiResponse(responseCode = "404", description = "No se encontró ninguna cita registrada bajo el identificador provisto.")
    })
    public ResponseEntity<EntityModel<Cita>> obtenerCitaPorId(
            @Parameter(description = "Identificador único correlativo de la cita (Long)", required = true) 
            @PathVariable Long id) {
        Cita cita = citaService.obtenerPorId(id);
        // Reemplazamos la creación manual por el uso del assembler
        return ResponseEntity.ok(assembler.toModel(cita));
    }

    @GetMapping
    @Operation(
        summary = "Listar la colección completa de citas", 
        description = "Retorna una lista completa wrapped en un modelo relacional Colección HATEOAS, proveyendo enlaces de auto-referencia para cada ítem de manera individual."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Colección estructurada recuperada con éxito.")
    })
    public ResponseEntity<CollectionModel<EntityModel<Cita>>> obtenerTodas() {
        List<Cita> citas = citaService.obtenerTodas();
        // Usamos el método heredado de RepresentationModelAssembler para transformar la colección completa
        CollectionModel<EntityModel<Cita>> modeloColeccion = assembler.toCollectionModel(citas);
        return ResponseEntity.ok(modeloColeccion);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar/Reprogramar parámetros de una cita", 
        description = "Permite modificar variables de la cita. El sistema valida las restricciones lógicas y mutaciones de estado prohibidas (RN-05)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cita modificada exitosamente."),
        @ApiResponse(responseCode = "400", description = "La solicitud rompe las restricciones temporales o de estado de negocio.")
    })
    public ResponseEntity<EntityModel<Cita>> actualizarCita(
            @Parameter(description = "ID de la cita que se desea actualizar", required = true) 
            @PathVariable Long id, 
            @RequestBody CitaRequestDTO citaDTO) {
        Cita citaActualizada = citaService.actualizarCita(id, citaDTO);
        // El assembler se encarga de estructurar el retorno homogéneamente
        return ResponseEntity.ok(assembler.toModel(citaActualizada));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Cancelar/Eliminar una cita de la agenda", 
        description = "Da de baja un agendamiento liberando de forma inmediata la ventana temporal y los cupos diarios del taller mecánico."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cita removida del registro correctamente (No Content)."),
        @ApiResponse(responseCode = "400", description = "La cita no se puede eliminar por restricciones operacionales de la orden de trabajo activa.")
    })
    public ResponseEntity<Void> eliminarCita(
            @Parameter(description = "ID de la cita que se desea anular", required = true) 
            @PathVariable Long id) {
        citaService.eliminarCita(id);
        return ResponseEntity.noContent().build();
    }
}