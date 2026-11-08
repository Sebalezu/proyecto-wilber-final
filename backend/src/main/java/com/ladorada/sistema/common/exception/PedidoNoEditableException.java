package com.ladorada.sistema.common.exception;

public class PedidoNoEditableException extends RuntimeException {

    public PedidoNoEditableException(String mensaje) {
        super(mensaje);
    }

    public PedidoNoEditableException(Long id, String estadoActual) {
        super(String.format("El pedido con ID %d no se puede editar porque su estado actual es %s. Solo se permiten modificaciones en estado PENDIENTE.", id, estadoActual));
    }
}
