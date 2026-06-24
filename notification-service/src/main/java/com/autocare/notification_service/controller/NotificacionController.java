package com.autocare.notification_service.controller;

import com.autocare.notification_service.dto.NotificacionRequestDTO;
import com.autocare.notification_service.model.Notificacion;
import com.autocare.notification_service.service.NotificacionService;
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

@Tag(name = "Módulo de Notificaciones", description = "Servicios REST para el envío, consulta y gestión de notificaciones del taller.")
@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping
    @Operation(
        summary = "Listar todas las notificaciones",
        description = "Retorna la colección completa de notificaciones registradas en el sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Colección obtenida con éxito.")
    })
    public ResponseEntity<CollectionModel<EntityModel<Notificacion>>> listar() {
        List<Notificacion> notificaciones = notificacionService.listarTodas();

        List<EntityModel<Notificacion>> recursos = new ArrayList<>();
        for (Notificacion n : notificaciones) {
            EntityModel<Notificacion> recurso = EntityModel.of(n);
            recurso.add(linkTo(methodOn(NotificacionController.class).buscarPorId(n.getIdNotificacion())).withSelfRel());
            recursos.add(recurso);
        }

        CollectionModel<EntityModel<Notificacion>> coleccion = CollectionModel.of(recursos);
        coleccion.add(linkTo(methodOn(NotificacionController.class).listar()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener notificación por ID",
        description = "Recupera una notificación específica mediante su UUID e inyecta enlaces de navegación."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notificación encontrada correctamente."),
        @ApiResponse(responseCode = "404", description = "No existe una notificación con ese ID.")
    })
    public ResponseEntity<EntityModel<Notificacion>> buscarPorId(
            @Parameter(description = "UUID de la notificación", required = true)
            @PathVariable String id) {
        Notificacion notificacion = notificacionService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada con ID: " + id));

        EntityModel<Notificacion> recurso = EntityModel.of(notificacion);
        recurso.add(linkTo(methodOn(NotificacionController.class).buscarPorId(id)).withSelfRel());
        recurso.add(linkTo(methodOn(NotificacionController.class).listar()).withRel("todas-las-notificaciones"));
        recurso.add(linkTo(methodOn(NotificacionController.class).marcarComoLeida(id)).withRel("marcar-como-leida"));
        return ResponseEntity.ok(recurso);
    }

    @GetMapping("/destinatario/{idDestinatario}")
    @Operation(
        summary = "Obtener notificaciones por destinatario",
        description = "Retorna todas las notificaciones asociadas a un destinatario específico."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notificaciones del destinatario obtenidas con éxito.")
    })
    public ResponseEntity<CollectionModel<EntityModel<Notificacion>>> buscarPorDestinatario(
            @Parameter(description = "ID del destinatario", required = true)
            @PathVariable String idDestinatario) {
        List<Notificacion> notificaciones = notificacionService.buscarPorDestinatario(idDestinatario);

        List<EntityModel<Notificacion>> recursos = new ArrayList<>();
        for (Notificacion n : notificaciones) {
            EntityModel<Notificacion> recurso = EntityModel.of(n);
            recurso.add(linkTo(methodOn(NotificacionController.class).buscarPorId(n.getIdNotificacion())).withSelfRel());
            recursos.add(recurso);
        }

        CollectionModel<EntityModel<Notificacion>> coleccion = CollectionModel.of(recursos);
        coleccion.add(linkTo(methodOn(NotificacionController.class).buscarPorDestinatario(idDestinatario)).withSelfRel());
        coleccion.add(linkTo(methodOn(NotificacionController.class).noLeidasPorDestinatario(idDestinatario)).withRel("no-leidas"));
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/destinatario/{idDestinatario}/no-leidas")
    @Operation(
        summary = "Obtener notificaciones no leídas por destinatario",
        description = "Filtra las notificaciones de un destinatario que aún no han sido marcadas como leídas."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notificaciones no leídas obtenidas con éxito.")
    })
    public ResponseEntity<CollectionModel<EntityModel<Notificacion>>> noLeidasPorDestinatario(
            @Parameter(description = "ID del destinatario", required = true)
            @PathVariable String idDestinatario) {
        List<Notificacion> notificaciones = notificacionService.buscarNoLeidasPorDestinatario(idDestinatario);

        List<EntityModel<Notificacion>> recursos = new ArrayList<>();
        for (Notificacion n : notificaciones) {
            EntityModel<Notificacion> recurso = EntityModel.of(n);
            recurso.add(linkTo(methodOn(NotificacionController.class).buscarPorId(n.getIdNotificacion())).withSelfRel());
            recurso.add(linkTo(methodOn(NotificacionController.class).marcarComoLeida(n.getIdNotificacion())).withRel("marcar-como-leida"));
            recursos.add(recurso);
        }

        CollectionModel<EntityModel<Notificacion>> coleccion = CollectionModel.of(recursos);
        coleccion.add(linkTo(methodOn(NotificacionController.class).noLeidasPorDestinatario(idDestinatario)).withSelfRel());
        coleccion.add(linkTo(methodOn(NotificacionController.class).buscarPorDestinatario(idDestinatario)).withRel("todas-del-destinatario"));
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/tipo/{tipo}")
    @Operation(
        summary = "Obtener notificaciones por tipo",
        description = "Filtra las notificaciones según el tipo de evento del sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notificaciones filtradas por tipo obtenidas con éxito.")
    })
    public ResponseEntity<CollectionModel<EntityModel<Notificacion>>> buscarPorTipo(
            @Parameter(description = "Tipo de notificación (ORDEN_CREADA, FACTURA_EMITIDA, etc.)", required = true)
            @PathVariable Notificacion.TipoNotificacion tipo) {
        List<Notificacion> notificaciones = notificacionService.buscarPorTipo(tipo);

        List<EntityModel<Notificacion>> recursos = new ArrayList<>();
        for (Notificacion n : notificaciones) {
            EntityModel<Notificacion> recurso = EntityModel.of(n);
            recurso.add(linkTo(methodOn(NotificacionController.class).buscarPorId(n.getIdNotificacion())).withSelfRel());
            recursos.add(recurso);
        }

        CollectionModel<EntityModel<Notificacion>> coleccion = CollectionModel.of(recursos);
        coleccion.add(linkTo(methodOn(NotificacionController.class).buscarPorTipo(tipo)).withSelfRel());
        coleccion.add(linkTo(methodOn(NotificacionController.class).listar()).withRel("todas-las-notificaciones"));
        return ResponseEntity.ok(coleccion);
    }

    @PostMapping
    @Operation(
        summary = "Enviar notificación",
        description = "Registra y envía una nueva notificación validando el DTO de entrada."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Notificación enviada con éxito."),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o incompletos en la solicitud.")
    })
    public ResponseEntity<EntityModel<Notificacion>> enviar(@Valid @RequestBody NotificacionRequestDTO dto) {
        Notificacion notificacion = mapearDtoAEntidad(dto);
        Notificacion guardada = notificacionService.enviar(notificacion);

        EntityModel<Notificacion> recurso = EntityModel.of(guardada);
        recurso.add(linkTo(methodOn(NotificacionController.class).buscarPorId(guardada.getIdNotificacion())).withSelfRel());
        recurso.add(linkTo(methodOn(NotificacionController.class).listar()).withRel("todas-las-notificaciones"));
        recurso.add(linkTo(methodOn(NotificacionController.class).marcarComoLeida(guardada.getIdNotificacion())).withRel("marcar-como-leida"));
        return ResponseEntity.status(HttpStatus.CREATED).body(recurso);
    }

    @PatchMapping("/{id}/leer")
    @Operation(
        summary = "Marcar notificación como leída",
        description = "Cambia el estado de una notificación de NO_LEIDA a LEIDA."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente."),
        @ApiResponse(responseCode = "404", description = "No se encontró la notificación.")
    })
    public ResponseEntity<EntityModel<Notificacion>> marcarComoLeida(
            @Parameter(description = "UUID de la notificación", required = true)
            @PathVariable String id) {
        Notificacion actualizada = notificacionService.marcarComoLeida(id);

        EntityModel<Notificacion> recurso = EntityModel.of(actualizada);
        recurso.add(linkTo(methodOn(NotificacionController.class).buscarPorId(id)).withSelfRel());
        recurso.add(linkTo(methodOn(NotificacionController.class).listar()).withRel("todas-las-notificaciones"));
        return ResponseEntity.ok(recurso);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar notificación por ID",
        description = "Elimina una notificación del sistema de forma permanente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Notificación eliminada correctamente."),
        @ApiResponse(responseCode = "404", description = "No se encontró la notificación.")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "UUID de la notificación", required = true)
            @PathVariable String id) {
        notificacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private Notificacion mapearDtoAEntidad(NotificacionRequestDTO dto) {
        Notificacion notificacion = new Notificacion();
        notificacion.setIdDestinatario(dto.getIdDestinatario());
        notificacion.setTipo(dto.getTipo());
        notificacion.setMensaje(dto.getMensaje());
        notificacion.setIdReferencia(dto.getIdReferencia());
        return notificacion;
    }
}
