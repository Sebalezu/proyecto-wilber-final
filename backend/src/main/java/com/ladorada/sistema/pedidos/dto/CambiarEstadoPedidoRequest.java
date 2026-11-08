package com.ladorada.sistema.pedidos.dto;

import com.ladorada.sistema.pedidos.model.EstadoPedido;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoPedidoRequest(
        @NotNull(message = "El estado es obligatorio")
        EstadoPedido estado
) {}
