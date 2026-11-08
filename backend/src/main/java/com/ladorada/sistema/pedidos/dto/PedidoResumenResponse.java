package com.ladorada.sistema.pedidos.dto;

import com.ladorada.sistema.pedidos.model.EstadoPedido;
import com.ladorada.sistema.pedidos.model.Pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoResumenResponse(
        Long id,
        ClienteResumenDto cliente,
        BigDecimal total,
        EstadoPedido estado,
        boolean stockDescontado,
        LocalDateTime fecha
) {
    public static PedidoResumenResponse fromEntity(Pedido pedido) {
        return new PedidoResumenResponse(
                pedido.getId(),
                ClienteResumenDto.fromEntity(pedido.getCliente()),
                pedido.getTotal(),
                pedido.getEstado(),
                pedido.isStockDescontado(),
                pedido.getCreatedAt()
        );
    }
}
