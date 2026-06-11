package com.autocare.checkin_service.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RecepcionRequestDTO {

    @NotBlank(message = "El id del vehículo es obligatorio")
    private String idVehiculo;

    @NotNull(message = "El kilometraje es obligatorio")
    @Min(value = 0, message = "El kilometraje no puede ser negativo")
    private Integer kilometrajeEntrada;

    @NotBlank(message = "El nivel de combustible es obligatorio")
    private String nivelCombustible;

    private String danosVisuales;

    private String objetosDejados;
}