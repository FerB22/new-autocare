package com.autocare.booking_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "Objeto de transferencia de datos (Payload) requerido para agendar o actualizar una cita mecánica.")
public record CitaRequestDTO(
        
        @Schema(description = "Identificador único correlativo del cliente dueño del vehículo.", example = "1")
        @NotNull Long clienteId,
        
        @Schema(description = "Identificador único correlativo del vehículo que recibirá el servicio.", example = "10")
        @NotNull Long vehiculoId,
        
        @Schema(description = "Fecha y hora exacta propuesta para la cita. Estrictamente debe ser una fecha en el futuro.", example = "2026-12-01T10:00:00")
        @NotNull @Future LocalDateTime fechaHora,
        
        @Schema(description = "Descripción detallada del motivo de la visita, diagnóstico preliminar o servicio requerido.", example = "Mantención por kilometraje y revisión de frenos")
        @NotBlank String motivo
) {}