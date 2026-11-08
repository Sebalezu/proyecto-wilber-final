package com.ladorada.sistema.common.exception;

public class CodigoDuplicadoException extends RuntimeException {

    public CodigoDuplicadoException(String mensaje) {
        super(mensaje);
    }

    public CodigoDuplicadoException(String recurso, String codigo) {
        super(String.format("Ya existe un %s con el código '%s'", recurso, codigo));
    }
}
