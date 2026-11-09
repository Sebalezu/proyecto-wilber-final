package com.ladorada.sistema.inventario.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AjusteInventarioRequest(
        @NotNull(message = "El ID del producto es obligatorio")
        Long productoId,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad a ingresar debe ser un número entero mayor a cero")
        Integer cantidad,

        String motivo
) {
}

