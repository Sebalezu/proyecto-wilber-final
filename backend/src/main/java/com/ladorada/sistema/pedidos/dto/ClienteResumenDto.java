package com.ladorada.sistema.pedidos.dto;

import com.ladorada.sistema.catalogo.model.Cliente;

public record ClienteResumenDto(
        Long id,
        String nombre,
        String telefono
) {
    public static ClienteResumenDto fromEntity(Cliente cliente) {
        if (cliente == null) {
            return null;
        }
        return new ClienteResumenDto(cliente.getId(), cliente.getNombre(), cliente.getTelefono());
    }
}

