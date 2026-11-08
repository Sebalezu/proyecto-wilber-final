package com.ladorada.sistema.pedidos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CrearPedidoRequest(
        @NotNull(message = "El clienteId es obligatorio")
        Long clienteId,

        Boolean confirmar,

        @NotEmpty(message = "El pedido debe tener al menos una línea")
        @Valid
        List<LineaPedidoRequest> lineas
) {
    public boolean debeConfirmar() {
        return Boolean.TRUE.equals(confirmar);
    }
}

