package com.autocare.fleet_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * DTO de entrada para crear o actualizar un vehículo.
 * Separa los datos de la API REST de la entidad JPA.
 * El idVehiculo se genera automáticamente; no se recibe del cliente.
 */
public class VehiculoRequestDTO {

    @NotBlank(message = "La patente es obligatoria")
    private String patente;

    @NotBlank(message = "La marca es obligatoria")
    private String marca;

    @NotBlank(message = "El modelo es obligatorio")
    private String modelo;

    @NotNull(message = "El año es obligatorio")
    @Min(value = 1900, message = "El año no puede ser menor a 1900")
    @Max(value = 2030, message = "El año no puede ser mayor a 2030")
    private Integer anio;

    // Campos opcionales (sin validación obligatoria)
    private String vinChasis;
    private String idDuenio;

    // Getters y Setters
    public String getPatente() { return patente; }
    public void setPatente(String patente) { this.patente = patente; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }

    public String getVinChasis() { return vinChasis; }
    public void setVinChasis(String vinChasis) { this.vinChasis = vinChasis; }

    public String getIdDuenio() { return idDuenio; }
    public void setIdDuenio(String idDuenio) { this.idDuenio = idDuenio; }
}