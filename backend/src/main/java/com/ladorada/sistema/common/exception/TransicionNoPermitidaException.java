package com.ladorada.sistema.common.exception;

public class TransicionNoPermitidaException extends RuntimeException {

    public TransicionNoPermitidaException(String mensaje) {
        super(mensaje);
    }

    public TransicionNoPermitidaException(String estadoAnterior, String estadoNuevo) {
        super(String.format("Transición no permitida: %s → %s", estadoAnterior, estadoNuevo));
    }
}

