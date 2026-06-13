package com.autocare.workshop_service.controller;

import com.autocare.workshop_service.dto.AsignarRepuestoDTO;
import com.autocare.workshop_service.dto.OrdenTrabajoRequestDTO;
import com.autocare.workshop_service.model.OrdenTrabajo;
import com.autocare.workshop_service.service.OrdenTrabajoService;
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

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/taller")
@RequiredArgsConstructor
@Tag(
    name = "Módulo de Órdenes de Trabajo",
    description = "Servicios REST para la creación, consulta y gestión de órdenes de trabajo del taller mecánico."
)
public class OrdenTrabajoController {

    private final OrdenTrabajoService service;

    @GetMapping
    @Operation(
        summary = "Listar todas las órdenes de trabajo",
        description = "Retorna la colección completa de órdenes de trabajo registradas en el taller."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Colección de órdenes obtenida con éxito.")
    })
    public ResponseEntity<CollectionModel<EntityModel<OrdenTrabajo>>> listarOrdenes() {
        List<OrdenTrabajo> ordenes = service.obtenerTodas();

        List<EntityModel<OrdenTrabajo>> recursos = new ArrayList<>();
        for (OrdenTrabajo orden : ordenes) {
            EntityModel<OrdenTrabajo> recurso = EntityModel.of(orden);
            recurso.add(linkTo(methodOn(OrdenTrabajoController.class).usarRepuesto(orden.getId(), null)).withRel("usar-repuesto"));
            recursos.add(recurso);
        }

        CollectionModel<EntityModel<OrdenTrabajo>> coleccion = CollectionModel.of(recursos);
        coleccion.add(linkTo(methodOn(OrdenTrabajoController.class).listarOrdenes()).withSelfRel());
        coleccion.add(linkTo(methodOn(OrdenTrabajoController.class).crearOrden(null)).withRel("crear-orden"));

        return ResponseEntity.ok(coleccion);
    }

    @PostMapping
    @Operation(
        summary = "Crear una orden de trabajo",
        description = "Registra una nueva orden de trabajo vinculando un vehículo y un mecánico, con la descripción de la falla."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Orden de trabajo creada con éxito."),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o entidades referenciadas inexistentes.")
    })
    public ResponseEntity<EntityModel<OrdenTrabajo>> crearOrden(@Valid @RequestBody OrdenTrabajoRequestDTO dto) {
        OrdenTrabajo nueva = service.crearOrden(dto);
        EntityModel<OrdenTrabajo> recurso = EntityModel.of(nueva);

        recurso.add(linkTo(methodOn(OrdenTrabajoController.class).crearOrden(null)).withSelfRel());
        recurso.add(linkTo(methodOn(OrdenTrabajoController.class).listarOrdenes()).withRel("todas-las-ordenes"));
        recurso.add(linkTo(methodOn(OrdenTrabajoController.class).usarRepuesto(nueva.getId(), null)).withRel("usar-repuesto"));

        return ResponseEntity.status(HttpStatus.CREATED).body(recurso);
    }

    @PostMapping("/{id}/usar-repuesto")
    @Operation(
        summary = "Asignar un repuesto a una orden de trabajo",
        description = "Descuenta el stock del inventario y registra el uso de un repuesto en la orden especificada."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Repuesto asignado correctamente a la orden."),
        @ApiResponse(responseCode = "400", description = "Stock insuficiente o datos inválidos."),
        @ApiResponse(responseCode = "404", description = "No se encontró la orden de trabajo o el repuesto.")
    })
    public ResponseEntity<EntityModel<OrdenTrabajo>> usarRepuesto(
            @Parameter(description = "ID de la orden de trabajo", required = true)
            @PathVariable Long id,
            @Valid @RequestBody AsignarRepuestoDTO dto) {
        OrdenTrabajo ordenActualizada = service.utilizarRepuestoEnOrden(id, dto);
        EntityModel<OrdenTrabajo> recurso = EntityModel.of(ordenActualizada);

        recurso.add(linkTo(methodOn(OrdenTrabajoController.class).usarRepuesto(id, null)).withSelfRel());
        recurso.add(linkTo(methodOn(OrdenTrabajoController.class).listarOrdenes()).withRel("todas-las-ordenes"));

        return ResponseEntity.ok(recurso);
    }
}