package com.autocare.garage_service.exception;

public class VehiculoNoEncontradoException extends RuntimeException {
    public VehiculoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
