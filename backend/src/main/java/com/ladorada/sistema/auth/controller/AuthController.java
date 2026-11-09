package com.ladorada.sistema.auth.controller;

import com.ladorada.sistema.auth.dto.LoginRequest;
import com.ladorada.sistema.auth.dto.UsuarioResponse;
import com.ladorada.sistema.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints para inicio de sesión y gestión de accesos")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Verifica credenciales contra la base de datos y retorna datos del usuario y rol asignado.")
    public ResponseEntity<UsuarioResponse> login(@Valid @RequestBody LoginRequest request) {
        UsuarioResponse usuario = authService.login(request);
        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/usuarios")
    @Operation(summary = "Listar usuarios activos", description = "Retorna lista de usuarios activos para selección rápida y pruebas.")
    public ResponseEntity<List<UsuarioResponse>> listarUsuariosActivos() {
        List<UsuarioResponse> usuarios = authService.listarUsuariosActivos();
        return ResponseEntity.ok(usuarios);
    }
}
