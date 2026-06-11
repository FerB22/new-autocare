package com.autocare.crm_service.dto;

import com.autocare.crm_service.model.Interaccion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class InteraccionRequestDTO {

    @NotBlank(message = "El id del cliente es obligatorio")
    private String idCliente;

    @NotNull(message = "El tipo de interacción es obligatorio")
    private Interaccion.TipoInteraccion tipo;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 10, message = "La descripción debe tener al menos 10 caracteres")
    private String descripcion;
}