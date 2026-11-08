package com.ladorada.sistema.catalogo.service;

import com.ladorada.sistema.catalogo.dto.ClienteRequest;
import com.ladorada.sistema.catalogo.dto.ClienteResponse;
import com.ladorada.sistema.catalogo.model.Cliente;
import com.ladorada.sistema.catalogo.repository.ClienteRepository;
import com.ladorada.sistema.common.exception.RecursoNoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente clienteExistente;

    @BeforeEach
    void setUp() {
        clienteExistente = new Cliente(1L, "Restaurante El Sabor", "+57 310 555 1234");
    }

    @Test
    void listar_sinFiltro_retornaTodos() {
        when(clienteRepository.findAll()).thenReturn(List.of(clienteExistente));

        List<ClienteResponse> resultado = clienteService.listar(null);

        assertEquals(1, resultado.size());
        assertEquals("Restaurante El Sabor", resultado.get(0).nombre());
        verify(clienteRepository).findAll();
    }

    @Test
    void listar_conFiltro_llamaBuscarPorNombre() {
        when(clienteRepository.findByNombreContainingIgnoreCase("Sabor"))
                .thenReturn(List.of(clienteExistente));

        List<ClienteResponse> resultado = clienteService.listar("Sabor");

        assertEquals(1, resultado.size());
        verify(clienteRepository).findByNombreContainingIgnoreCase("Sabor");
    }

    @Test
    void obtenerPorId_existente_retornaCliente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente));

        ClienteResponse resultado = clienteService.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("Restaurante El Sabor", resultado.nombre());
    }

    @Test
    void obtenerPorId_inexistente_lanzaExcepcion() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> clienteService.obtenerPorId(99L));
    }

    @Test
    void crear_datosValidos_guardaYRetorna() {
        ClienteRequest request = new ClienteRequest("Nuevo Minimarket", "+57 300 111 2233");
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente c = invocation.getArgument(0);
            c.setId(2L);
            return c;
        });

        ClienteResponse creado = clienteService.crear(request);

        assertNotNull(creado);
        assertEquals(2L, creado.id());
        assertEquals("Nuevo Minimarket", creado.nombre());
    }

    @Test
    void actualizar_existente_modificaCampos() {
        ClienteRequest request = new ClienteRequest("Restaurante Actualizado", "+57 311 000 0000");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClienteResponse actualizado = clienteService.actualizar(1L, request);

        assertEquals("Restaurante Actualizado", actualizado.nombre());
        assertEquals("+57 311 000 0000", actualizado.telefono());
    }
}
