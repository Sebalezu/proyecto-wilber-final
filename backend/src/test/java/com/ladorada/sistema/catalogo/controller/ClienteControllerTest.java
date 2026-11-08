package com.ladorada.sistema.catalogo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ladorada.sistema.catalogo.dto.ClienteRequest;
import com.ladorada.sistema.catalogo.dto.ClienteResponse;
import com.ladorada.sistema.catalogo.service.ClienteService;
import com.ladorada.sistema.common.exception.GlobalExceptionHandler;
import com.ladorada.sistema.common.exception.RecursoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
@Import(GlobalExceptionHandler.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClienteService clienteService;

    @Test
    void listar_retorna200YLista() throws Exception {
        when(clienteService.listar(null)).thenReturn(List.of(
                new ClienteResponse(1L, "Restaurante El Sabor", "+57 310 555 1234")
        ));

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Restaurante El Sabor"));
    }

    @Test
    void obtenerPorId_existente_retorna200() throws Exception {
        when(clienteService.obtenerPorId(1L)).thenReturn(
                new ClienteResponse(1L, "Restaurante El Sabor", "+57 310 555 1234")
        );

        mockMvc.perform(get("/api/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Restaurante El Sabor"));
    }

    @Test
    void obtenerPorId_inexistente_retorna404() throws Exception {
        when(clienteService.obtenerPorId(99L)).thenThrow(new RecursoNoEncontradoException("Cliente", 99L));

        mockMvc.perform(get("/api/clientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void crear_datosValidos_retorna201() throws Exception {
        ClienteRequest request = new ClienteRequest("Nuevo Minimarket", "+57 300 111 2233");
        when(clienteService.crear(any(ClienteRequest.class))).thenReturn(
                new ClienteResponse(2L, "Nuevo Minimarket", "+57 300 111 2233")
        );

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.nombre").value("Nuevo Minimarket"));
    }

    @Test
    void crear_nombreEnBlanco_retorna400() throws Exception {
        ClienteRequest request = new ClienteRequest("   ", "+57 300 111 2233");

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].campo").value("nombre"));
    }

    @Test
    void actualizar_datosValidos_retorna200() throws Exception {
        ClienteRequest request = new ClienteRequest("Nombre Actualizado", "+57 300 999 8877");
        when(clienteService.actualizar(eq(1L), any(ClienteRequest.class))).thenReturn(
                new ClienteResponse(1L, "Nombre Actualizado", "+57 300 999 8877")
        );

        mockMvc.perform(put("/api/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nombre Actualizado"));
    }
}
