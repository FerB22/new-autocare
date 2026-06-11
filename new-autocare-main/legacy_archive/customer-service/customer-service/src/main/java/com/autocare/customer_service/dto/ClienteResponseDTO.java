package com.autocare.customer_service.dto;

import lombok.Data;

/**
 * DTO de salida que representa los datos de un Cliente
 * que se envían como respuesta al cliente HTTP.
 *
 * Al usar este DTO en lugar de la entidad Cliente directamente,
 * controlamos exactamente qué información se expone en la API,
 * separando la capa de persistencia de la capa de presentación.
 */
@Data
public class ClienteResponseDTO {

    // Se incluye el ID porque el cliente que consume la API
    // lo necesita para futuras operaciones (PUT, DELETE, búsquedas).
    private String idCliente;

    private String nombre;

    private String apellido;

    private String email;

    private String telefono;

    private String direccion;
}