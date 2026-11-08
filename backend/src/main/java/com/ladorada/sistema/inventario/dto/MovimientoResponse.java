package com.ladorada.sistema.inventario.dto;

import com.ladorada.sistema.inventario.model.MovimientoInventario;
import com.ladorada.sistema.inventario.model.TipoMovimiento;

import java.time.LocalDateTime;

public record MovimientoResponse(
        Long id,
        Long productoId,
        Long pedidoId,
        TipoMovimiento tipo,
        int cantidad,
        LocalDateTime fecha
) {
    public static MovimientoResponse fromEntity(MovimientoInventario movimiento) {
        return new MovimientoResponse(
                movimiento.getId(),
                movimiento.getProductoId(),
                movimiento.getPedidoId(),
                movimiento.getTipo(),
                movimiento.getCantidad(),
                movimiento.getFecha()
        );
    }
}
