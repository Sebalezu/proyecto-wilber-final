package com.ladorada.sistema.common.exception;

public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public RecursoNoEncontradoException(String recurso, Long id) {
        super(String.format("%s con ID %d no encontrado", recurso, id));
    }
}
