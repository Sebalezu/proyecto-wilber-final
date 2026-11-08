package com.ladorada.sistema.catalogo.dto;

import com.ladorada.sistema.catalogo.model.Producto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductoResponse(
        Long id,
        String codigo,
        String nombre,
        BigDecimal precio,
        int existencias,
        boolean activo,
        boolean stockBajo,
        LocalDateTime createdAt
) {
    public static ProductoResponse fromEntity(Producto p, int umbralStockBajo) {
        return new ProductoResponse(
                p.getId(),
                p.getCodigo(),
                p.getNombre(),
                p.getPrecio(),
                p.getExistencias(),
                p.isActivo(),
                p.getExistencias() <= umbralStockBajo,
                p.getCreatedAt()
        );
    }
}
