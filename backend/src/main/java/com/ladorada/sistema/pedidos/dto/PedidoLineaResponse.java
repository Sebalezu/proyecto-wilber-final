package com.ladorada.sistema.pedidos.dto;

import com.ladorada.sistema.pedidos.model.PedidoLinea;

import java.math.BigDecimal;

public record PedidoLineaResponse(
        Long id,
        Long productoId,
        String productoNombre,
        String productoCodigo,
        int cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {
    public static PedidoLineaResponse fromEntity(PedidoLinea linea) {
        return new PedidoLineaResponse(
                linea.getId(),
                linea.getProducto() != null ? linea.getProducto().getId() : null,
                linea.getProducto() != null ? linea.getProducto().getNombre() : null,
                linea.getProducto() != null ? linea.getProducto().getCodigo() : null,
                linea.getCantidad(),
                linea.getPrecioUnitario(),
                linea.getSubtotal()
        );
    }
}

