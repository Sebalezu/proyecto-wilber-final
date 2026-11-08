package com.ladorada.sistema.catalogo.dto;

import com.ladorada.sistema.catalogo.model.Cliente;

public record ClienteResponse(
        Long id,
        String nombre,
        String telefono
) {
    public static ClienteResponse fromEntity(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getTelefono()
        );
    }
}
