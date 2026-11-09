package com.ladorada.sistema.auth;

import com.ladorada.sistema.auth.dto.LoginRequest;
import com.ladorada.sistema.auth.dto.UsuarioResponse;
import com.ladorada.sistema.auth.model.RolUsuario;
import com.ladorada.sistema.auth.model.Usuario;
import com.ladorada.sistema.auth.repository.UsuarioRepository;
import com.ladorada.sistema.auth.service.AuthService;
import com.ladorada.sistema.common.exception.CredencialesInvalidasException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AuthService authService;

    private Usuario usuarioActivo;

    @BeforeEach
    void setUp() {
        usuarioActivo = new Usuario(1L, "Admin General", "admin@ladorada.com", "admin123", RolUsuario.ADMINISTRADOR, true);
    }

    @Test
    @DisplayName("Debe autenticar correctamente con credenciales válidas")
    void loginExitoso() {
        when(usuarioRepository.findByEmail("admin@ladorada.com")).thenReturn(Optional.of(usuarioActivo));

        LoginRequest request = new LoginRequest("admin@ladorada.com", "admin123");
        UsuarioResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Admin General", response.nombre());
        assertEquals("admin@ladorada.com", response.email());
        assertEquals(RolUsuario.ADMINISTRADOR, response.rol());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el correo no existe")
    void loginEmailInexistente() {
        when(usuarioRepository.findByEmail("noexiste@ladorada.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("noexiste@ladorada.com", "admin123");

        assertThrows(CredencialesInvalidasException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Debe lanzar excepción si la contraseña es incorrecta")
    void loginPasswordIncorrecto() {
        when(usuarioRepository.findByEmail("admin@ladorada.com")).thenReturn(Optional.of(usuarioActivo));

        LoginRequest request = new LoginRequest("admin@ladorada.com", "clave_errada");

        assertThrows(CredencialesInvalidasException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el usuario se encuentra inactivo")
    void loginUsuarioInactivo() {
        Usuario inactivo = new Usuario(2L, "Usuario Inactivo", "inactivo@ladorada.com", "clave123", RolUsuario.VENDEDOR, false);
        when(usuarioRepository.findByEmail("inactivo@ladorada.com")).thenReturn(Optional.of(inactivo));

        LoginRequest request = new LoginRequest("inactivo@ladorada.com", "clave123");

        assertThrows(CredencialesInvalidasException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Debe listar usuarios activos correctamente")
    void listarUsuariosActivos() {
        Usuario u2 = new Usuario(2L, "Vendedor Uno", "vendedor@ladorada.com", "v123", RolUsuario.VENDEDOR, true);
        when(usuarioRepository.findByActivoTrue()).thenReturn(List.of(usuarioActivo, u2));

        List<UsuarioResponse> lista = authService.listarUsuariosActivos();

        assertEquals(2, lista.size());
        assertEquals("admin@ladorada.com", lista.get(0).email());
        assertEquals("vendedor@ladorada.com", lista.get(1).email());
    }
}
