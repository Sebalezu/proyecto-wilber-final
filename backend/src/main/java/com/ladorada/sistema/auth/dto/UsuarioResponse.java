package com.ladorada.sistema.auth.dto;

import com.ladorada.sistema.auth.model.RolUsuario;
import com.ladorada.sistema.auth.model.Usuario;

public record UsuarioResponse(
        Long id,
        String nombre,
        String email,
        RolUsuario rol
) {
    public static UsuarioResponse fromEntity(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol()
        );
    }
}

