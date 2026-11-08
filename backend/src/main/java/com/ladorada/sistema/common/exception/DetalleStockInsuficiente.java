package com.ladorada.sistema.common.exception;

public record DetalleStockInsuficiente(
        Long productoId,
        String nombre,
        int solicitado,
        int disponible
) {}
