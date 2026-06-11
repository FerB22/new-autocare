package com.autocare.notification_service.controller;

import com.autocare.notification_service.dto.NotificacionRequestDTO;
import com.autocare.notification_service.model.Notificacion;
import com.autocare.notification_service.service.NotificacionService;
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

@Tag(name = "Notificaciones", description = "Gestión de notificaciones del taller: envío, consulta y seguimiento de lectura")
@RestController
@RequestMapping("/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @Operation(
        summary = "Listar todas las notificaciones",
        description = "Retorna una lista con todas las notificaciones registradas en el sistema, sin importar su estado o destinatario."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de notificaciones obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<List<Notificacion>> listar() {
        return ResponseEntity.ok(notificacionService.listarTodas());
    }

    @Operation(
        summary = "Obtener notificación por ID",
        description = "Busca y retorna una notificación específica por su identificador único. Retorna 404 si no existe."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notificación encontrada y retornada exitosamente"),
        @ApiResponse(responseCode = "404", description = "No existe una notificación con el ID proporcionado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Object> buscarPorId(@PathVariable String id) {
        Optional<Notificacion> resultado = notificacionService.buscarPorId(id);
        if (resultado.isPresent()) {
            return ResponseEntity.ok(resultado.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Notificación no encontrada con ID: " + id));
    }

    @Operation(
        summary = "Obtener notificaciones por destinatario",
        description = "Retorna todas las notificaciones asociadas a un destinatario específico, sin importar si fueron leídas o no."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de notificaciones del destinatario obtenida exitosamente")
    })
    @GetMapping("/destinatario/{idDestinatario}")
    public ResponseEntity<List<Notificacion>> buscarPorDestinatario(@PathVariable String idDestinatario) {
        return ResponseEntity.ok(notificacionService.buscarPorDestinatario(idDestinatario));
    }

    @Operation(
        summary = "Obtener notificaciones no leídas por destinatario",
        description = "Retorna únicamente las notificaciones con estado NO_LEIDA para un destinatario específico. El filtro se aplica directamente en base de datos para mayor eficiencia."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de notificaciones no leídas obtenida exitosamente")
    })
    @GetMapping("/destinatario/{idDestinatario}/no-leidas")
    public ResponseEntity<List<Notificacion>> noLeidasPorDestinatario(@PathVariable String idDestinatario) {
        return ResponseEntity.ok(notificacionService.buscarNoLeidasPorDestinatario(idDestinatario));
    }

    @Operation(
        summary = "Obtener notificaciones por tipo",
        description = "Filtra y retorna todas las notificaciones que coincidan con el tipo indicado (ej: CITA, FACTURA, DIAGNOSTICO)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de notificaciones filtradas por tipo obtenida exitosamente"),
        @ApiResponse(responseCode = "400", description = "El valor del tipo proporcionado no es válido")
    })
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<Object> buscarPorTipo(@PathVariable Notificacion.TipoNotificacion tipo) {
        return ResponseEntity.ok(notificacionService.buscarPorTipo(tipo));
    }

    @Operation(
        summary = "Enviar una notificación",
        description = "Crea y persiste una nueva notificación. La fecha de envío y el estado inicial (NO_LEIDA) se asignan automáticamente por el sistema, sin importar lo que envíe el cliente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Notificación enviada y persistida exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos del request inválidos o error de negocio al enviar la notificación")
    })
    @PostMapping
    public ResponseEntity<Object> enviar(@Valid @RequestBody NotificacionRequestDTO dto) {
        try {
            Notificacion notificacion = mapearDtoAEntidad(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(notificacionService.enviar(notificacion));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
        summary = "Marcar notificación como leída",
        description = "Actualiza el estado de una notificación a LEIDA. Solo aplica si la notificación existe en el sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notificación marcada como leída exitosamente"),
        @ApiResponse(responseCode = "404", description = "No existe una notificación con el ID proporcionado")
    })
    @PatchMapping("/{id}/leer")
    public ResponseEntity<Object> marcarComoLeida(@PathVariable String id) {
        try {
            return ResponseEntity.ok(notificacionService.marcarComoLeida(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
        summary = "Eliminar notificación por ID",
        description = "Elimina permanentemente una notificación del sistema. Falla con 404 si no existe una notificación con el ID indicado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Notificación eliminada exitosamente, sin contenido en la respuesta"),
        @ApiResponse(responseCode = "404", description = "No existe una notificación con el ID proporcionado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> eliminar(@PathVariable String id) {
        try {
            notificacionService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
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