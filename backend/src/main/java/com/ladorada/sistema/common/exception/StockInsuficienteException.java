package com.ladorada.sistema.common.exception;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class StockInsuficienteException extends RuntimeException {

    private final List<DetalleStockInsuficiente> detalles;

    public StockInsuficienteException(List<DetalleStockInsuficiente> detalles) {
        super(construirMensaje(detalles));
        this.detalles = detalles != null ? List.copyOf(detalles) : Collections.emptyList();
    }

    public StockInsuficienteException(DetalleStockInsuficiente detalle) {
        this(Collections.singletonList(detalle));
    }

    private static String construirMensaje(List<DetalleStockInsuficiente> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            return "Stock insuficiente para procesar la operación";
        }
        return detalles.stream()
                .map(d -> String.format("%s: pediste %d y hay %d disponibles",
                        d.nombre(), d.solicitado(), d.disponible()))
                .collect(Collectors.joining("; "));
    }

    public List<DetalleStockInsuficiente> getDetalles() {
        return detalles;
    }
}
