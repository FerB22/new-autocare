package com.autocare.loyalty_service.exception;

public class TransaccionInvalidaException extends RuntimeException {
    public TransaccionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
