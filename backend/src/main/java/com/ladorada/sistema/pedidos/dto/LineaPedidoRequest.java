package com.ladorada.sistema.pedidos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LineaPedidoRequest(
        @NotNull(message = "El ID del producto es obligatorio")
        Long productoId,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser un número entero mayor que cero.")
        Integer cantidad
) {}

