package com.autocare.workflow_service.dto;

import jakarta.validation.constraints.NotBlank;

public class OrdenTrabajoRequestDto {

    @NotBlank(message = "El ID del vehículo es obligatorio")
    private String idVehiculo;

    @NotBlank(message = "La prioridad es obligatoria")
    private String prioridad;

    public String getIdVehiculo() {
        return idVehiculo;
    }

    public void setIdVehiculo(String idVehiculo) {
        this.idVehiculo = idVehiculo;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }
}