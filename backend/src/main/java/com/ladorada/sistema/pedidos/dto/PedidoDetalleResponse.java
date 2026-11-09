package com.ladorada.sistema.pedidos.dto;

import com.ladorada.sistema.pedidos.model.EstadoPedido;
import com.ladorada.sistema.pedidos.model.Pedido;
import com.ladorada.sistema.pedidos.model.PedidoHistorialEstado;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public record PedidoDetalleResponse(
        Long id,
        ClienteResumenDto cliente,
        List<PedidoLineaResponse> lineas,
        BigDecimal total,
        EstadoPedido estado,
        boolean stockDescontado,
        LocalDateTime fecha,
        List<HistorialEstadoResponse> historial
) {
    public static PedidoDetalleResponse fromEntity(Pedido pedido, List<PedidoHistorialEstado> historial) {
        List<PedidoLineaResponse> lineasDto = pedido.getLineas() != null
                ? pedido.getLineas().stream().map(PedidoLineaResponse::fromEntity).toList()
                : Collections.emptyList();

        List<HistorialEstadoResponse> historialDto = historial != null
                ? historial.stream().map(HistorialEstadoResponse::fromEntity).toList()
                : Collections.emptyList();

        return new PedidoDetalleResponse(
                pedido.getId(),
                ClienteResumenDto.fromEntity(pedido.getCliente()),
                lineasDto,
                pedido.getTotal(),
                pedido.getEstado(),
                pedido.isStockDescontado(),
                pedido.getCreatedAt(),
                historialDto
        );
    }
}

