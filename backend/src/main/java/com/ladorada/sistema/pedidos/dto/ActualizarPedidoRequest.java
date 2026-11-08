package com.ladorada.sistema.pedidos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ActualizarPedidoRequest(
        @NotEmpty(message = "El pedido debe tener al menos una línea")
        @Valid
        List<LineaPedidoRequest> lineas
) {}

