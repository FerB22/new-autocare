package com.autocare.garage_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VehiculoRequestDTO(
        @NotBlank String patente,
        @NotBlank String marca,
        @NotBlank String modelo,
        @NotNull Integer anio,
        String color,
        String vin
) {}