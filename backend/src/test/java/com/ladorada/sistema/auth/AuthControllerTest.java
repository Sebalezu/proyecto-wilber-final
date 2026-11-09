package com.ladorada.sistema.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ladorada.sistema.auth.controller.AuthController;
import com.ladorada.sistema.auth.dto.LoginRequest;
import com.ladorada.sistema.auth.dto.UsuarioResponse;
import com.ladorada.sistema.auth.model.RolUsuario;
import com.ladorada.sistema.auth.service.AuthService;
import com.ladorada.sistema.common.exception.CredencialesInvalidasException;
import com.ladorada.sistema.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/auth/login retorna 200 OK con datos de usuario si las credenciales son válidas")
    void login_retorna200OK() throws Exception {
        UsuarioResponse response = new UsuarioResponse(1L, "Admin General", "admin@ladorada.com", RolUsuario.ADMINISTRADOR);
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        LoginRequest request = new LoginRequest("admin@ladorada.com", "admin123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Admin General"))
                .andExpect(jsonPath("$.email").value("admin@ladorada.com"))
                .andExpect(jsonPath("$.rol").value("ADMINISTRADOR"));
    }

    @Test
    @DisplayName("POST /api/auth/login retorna 401 Unauthorized si las credenciales son incorrectas")
    void login_retorna401Unauthorized() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new CredencialesInvalidasException("Credenciales inválidas o usuario inactivo"));

        LoginRequest request = new LoginRequest("admin@ladorada.com", "password_invalido");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Credenciales inválidas o usuario inactivo"))
                .andExpect(jsonPath("$.path").value("/api/auth/login"));
    }

    @Test
    @DisplayName("GET /api/auth/usuarios retorna 200 OK y la lista de usuarios activos")
    void listarUsuarios_retorna200() throws Exception {
        when(authService.listarUsuariosActivos()).thenReturn(List.of(
                new UsuarioResponse(1L, "Admin General", "admin@ladorada.com", RolUsuario.ADMINISTRADOR),
                new UsuarioResponse(2L, "Carlos Ventas", "ventas@ladorada.com", RolUsuario.VENDEDOR)
        ));

        mockMvc.perform(get("/api/auth/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].email").value("admin@ladorada.com"))
                .andExpect(jsonPath("$[1].email").value("ventas@ladorada.com"));
    }
}

