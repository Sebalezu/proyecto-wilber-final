package com.ladorada.sistema.common.exception;

public class PedidoNoEditableException extends RuntimeException {

    public PedidoNoEditableException(String mensaje) {
        super(mensaje);
    }

    public PedidoNoEditableException(Long id, String estadoActual) {
        super(String.format("Solo se pueden editar pedidos en estado PENDIENTE (estado actual: %s)", estadoActual));
    }
}
