package com.ladorada.sistema.auth.service;

import com.ladorada.sistema.auth.dto.LoginRequest;
import com.ladorada.sistema.auth.dto.UsuarioResponse;
import com.ladorada.sistema.auth.model.Usuario;
import com.ladorada.sistema.auth.repository.UsuarioRepository;
import com.ladorada.sistema.common.exception.CredencialesInvalidasException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UsuarioRepository usuarioRepository;

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioResponse login(LoginRequest request) {
        if (request.email() == null || request.password() == null) {
            throw new CredencialesInvalidasException("Credenciales inválidas o usuario inactivo");
        }

        Usuario usuario = usuarioRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new CredencialesInvalidasException("Credenciales inválidas o usuario inactivo"));

        if (!usuario.isActivo() || !usuario.getPassword().equals(request.password())) {
            throw new CredencialesInvalidasException("Credenciales inválidas o usuario inactivo");
        }

        return UsuarioResponse.fromEntity(usuario);
    }

    public List<UsuarioResponse> listarUsuariosActivos() {
        return usuarioRepository.findByActivoTrue().stream()
                .map(UsuarioResponse::fromEntity)
                .toList();
    }
}
