package com.autocare.booking_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO de sólo lectura que representa el vehículo enlazado a una cita, validado contra el Fleet Service.")
public class VehiculoDTO {

    @Schema(description = "Identificador único correlativo del vehículo.", example = "25")
    @NotNull(message = "ID del vehículo es obligatorio")
    private Long id;

    @Schema(description = "Marca fabricante del automóvil.", example = "Toyota")
    @NotBlank(message = "Marca es obligatoria")
    @Size(max = 30, message = "Marca no puede exceder 30 caracteres")
    private String marca;

    @Schema(description = "Modelo y línea del automóvil.", example = "Corolla Cross")
    @NotBlank(message = "Modelo es obligatorio")
    @Size(max = 50, message = "Modelo no puede exceder 50 caracteres")
    private String modelo;

    @Schema(description = "Placa patente del vehículo. Exige formato único chileno (4 letras y 2/3 números).", example = "ABCD12")
    @NotBlank(message = "Patente es obligatoria")
    @Pattern(regexp = "^[A-Z]{4}[0-9]{2,3}$", 
             message = "Patente debe tener formato chileno (ABCD123 o ABCD12)")
    private String patente;

    // Getters y setters (se mantienen exactamente igual)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getPatente() { return patente; }
    public void setPatente(String patente) { this.patente = patente; }

    @Schema(description = "String combinada para lectura rápida en frontend.", example = "Toyota Corolla Cross - ABCD12")
    public String getDescripcionCompleta() {
        return String.format("%s %s - %s", marca, modelo, patente);
    }
}