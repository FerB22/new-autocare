package com.autocare.booking_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(description = "DTO de sólo lectura para visualizar la información básica del cliente cruzada desde el Customer Service.")
public class ClienteDTO {

    @Schema(description = "ID único correlativo del cliente.", example = "1")
    @NotNull(message = "ID del cliente es obligatorio")
    private Long idCliente;

    @Schema(description = "Primer nombre del cliente.", example = "Fernando")
    @NotBlank(message = "Nombre es obligatorio")
    private String nombre;

    @Schema(description = "Apellido paterno del cliente.", example = "Barra")
    @NotBlank(message = "Apellido es obligatorio")
    private String apellido;

    @Schema(description = "Dirección de correo electrónico de contacto principal.", example = "contacto@ejemplo.com")
    @NotBlank(message = "Email es obligatorio")
    @Email(message = "Email debe tener formato válido")
    private String email;

    @Schema(description = "Número telefónico móvil con formato chileno (+569).", example = "+56987654321")
    @Pattern(regexp = "^(\\+56|0)?9\\d{8}$", 
             message = "Teléfono debe tener formato chileno válido (+569XXXXXXXX o 9XXXXXXXX)")
    private String telefono;

    // Getters y setters (se mantienen exactamente igual)
    public Long getIdCliente() { return idCliente; }
    public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    @Schema(description = "Computed property que une nombre y apellido. No se persiste en base de datos.", example = "Fernando Barra")
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}