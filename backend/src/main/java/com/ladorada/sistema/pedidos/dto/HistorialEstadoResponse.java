package com.ladorada.sistema.pedidos.dto;

import com.ladorada.sistema.pedidos.model.EstadoPedido;
import com.ladorada.sistema.pedidos.model.PedidoHistorialEstado;

import java.time.LocalDateTime;

public record HistorialEstadoResponse(
        Long id,
        EstadoPedido estadoAnterior,
        EstadoPedido estadoNuevo,
        LocalDateTime fecha
) {
    public static HistorialEstadoResponse fromEntity(PedidoHistorialEstado historial) {
        return new HistorialEstadoResponse(
                historial.getId(),
                historial.getEstadoAnterior(),
                historial.getEstadoNuevo(),
                historial.getFecha()
        );
    }
}
