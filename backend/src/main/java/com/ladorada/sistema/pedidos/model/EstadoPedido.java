package com.ladorada.sistema.pedidos.model;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public enum EstadoPedido {
    PENDIENTE,
    CONFIRMADO,
    DESPACHADO,
    RECHAZADO,
    CANCELADO;

    private static final Map<EstadoPedido, Set<EstadoPedido>> TRANSICIONES_PERMITIDAS = Map.of(
            PENDIENTE, Set.of(CONFIRMADO, RECHAZADO, CANCELADO),
            CONFIRMADO, Set.of(DESPACHADO, CANCELADO),
            DESPACHADO, Collections.emptySet(),
            RECHAZADO, Collections.emptySet(),
            CANCELADO, Collections.emptySet()
    );

    public boolean puedeTransicionarA(EstadoPedido nuevoEstado) {
        if (nuevoEstado == null) {
            return false;
        }
        return TRANSICIONES_PERMITIDAS.getOrDefault(this, Collections.emptySet()).contains(nuevoEstado);
    }

    public Set<EstadoPedido> getTransicionesPermitidas() {
        return TRANSICIONES_PERMITIDAS.getOrDefault(this, Collections.emptySet());
    }

    public boolean esEstadoFinal() {
        return this == DESPACHADO || this == RECHAZADO || this == CANCELADO;
    }
}
