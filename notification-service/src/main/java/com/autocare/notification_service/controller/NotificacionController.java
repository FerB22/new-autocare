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
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Notificaciones", description = "Gestión de notificaciones del taller")
@RestController
@RequestMapping("/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @Operation(summary = "Listar todas las notificaciones")
    @GetMapping
    public ResponseEntity<List<Notificacion>> listar() {
        return ResponseEntity.ok(notificacionService.listarTodas());
    }

    @Operation(summary = "Obtener notificación por ID")
    @GetMapping("/{id}")
    public ResponseEntity<Object> buscarPorId(@PathVariable String id) {
        Optional<Notificacion> resultado = notificacionService.buscarPorId(id);

        if (resultado.isPresent()) {
            return ResponseEntity.ok(resultado.get());
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Notificación no encontrada con ID: " + id));
    }

    @Operation(summary = "Obtener notificaciones por destinatario")
    @GetMapping("/destinatario/{idDestinatario}")
    public ResponseEntity<List<Notificacion>> buscarPorDestinatario(@PathVariable String idDestinatario) {
        return ResponseEntity.ok(notificacionService.buscarPorDestinatario(idDestinatario));
    }

    @Operation(summary = "Obtener notificaciones no leídas por destinatario")
    @GetMapping("/destinatario/{idDestinatario}/no-leidas")
    public ResponseEntity<List<Notificacion>> noLeidasPorDestinatario(@PathVariable String idDestinatario) {
        return ResponseEntity.ok(notificacionService.buscarNoLeidasPorDestinatario(idDestinatario));
    }

    @Operation(summary = "Obtener notificaciones por tipo")
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<Object> buscarPorTipo(@PathVariable Notificacion.TipoNotificacion tipo) {
        return ResponseEntity.ok(notificacionService.buscarPorTipo(tipo));
    }

    @Operation(summary = "Enviar notificación")
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

    @Operation(summary = "Marcar notificación como leída")
    @PatchMapping("/{id}/leer")
    public ResponseEntity<Object> marcarComoLeida(@PathVariable String id) {
        try {
            return ResponseEntity.ok(notificacionService.marcarComoLeida(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Eliminar por ID")
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