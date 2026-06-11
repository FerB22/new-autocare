package com.autocare.booking_service.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CitaRequestDTO(
        @NotNull Long clienteId,
        @NotNull Long vehiculoId,
        @NotNull @Future LocalDateTime fechaHora,
        @NotBlank String motivo
) {}