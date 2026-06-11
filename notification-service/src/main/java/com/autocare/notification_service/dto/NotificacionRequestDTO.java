package com.autocare.notification_service.dto;

import com.autocare.notification_service.model.Notificacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NotificacionRequestDTO {

    @NotBlank(message = "El destinatario es obligatorio")
    private String idDestinatario;

    @NotNull(message = "El tipo es obligatorio")
    private Notificacion.TipoNotificacion tipo;

    @NotBlank(message = "El mensaje es obligatorio")
    private String mensaje;

    private String idReferencia;
}