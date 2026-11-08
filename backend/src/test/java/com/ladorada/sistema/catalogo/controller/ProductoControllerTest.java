package com.ladorada.sistema.catalogo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ladorada.sistema.catalogo.dto.ProductoActivoRequest;
import com.ladorada.sistema.catalogo.dto.ProductoRequest;
import com.ladorada.sistema.catalogo.dto.ProductoResponse;
import com.ladorada.sistema.catalogo.dto.ProductoUpdateRequest;
import com.ladorada.sistema.catalogo.service.ProductoService;
import com.ladorada.sistema.common.exception.CodigoDuplicadoException;
import com.ladorada.sistema.common.exception.GlobalExceptionHandler;
import com.ladorada.sistema.common.exception.RecursoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductoController.class)
@Import(GlobalExceptionHandler.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductoService productoService;

    @Test
    void listar_retorna200YLista() throws Exception {
        when(productoService.listar(null, null)).thenReturn(List.of(
                new ProductoResponse(1L, "DET-001", "Detergente 5L", new BigDecimal("38500.00"), 12, true, false, LocalDateTime.now())
        ));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("DET-001"))
                .andExpect(jsonPath("$[0].stockBajo").value(false));
    }

    @Test
    void obtenerPorId_existente_retorna200() throws Exception {
        when(productoService.obtenerPorId(1L)).thenReturn(
                new ProductoResponse(1L, "DET-001", "Detergente 5L", new BigDecimal("38500.00"), 5, true, true, LocalDateTime.now())
        );

        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.stockBajo").value(true));
    }

    @Test
    void obtenerPorId_inexistente_retorna404() throws Exception {
        when(productoService.obtenerPorId(99L)).thenThrow(new RecursoNoEncontradoException("Producto", 99L));

        mockMvc.perform(get("/api/productos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void crear_datosValidos_retorna201() throws Exception {
        ProductoRequest request = new ProductoRequest("DET-001", "Detergente 5L", new BigDecimal("38500.00"), 12);
        when(productoService.crear(any(ProductoRequest.class))).thenReturn(
                new ProductoResponse(1L, "DET-001", "Detergente 5L", new BigDecimal("38500.00"), 12, true, false, LocalDateTime.now())
        );

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("DET-001"))
                .andExpect(jsonPath("$.nombre").value("Detergente 5L"));
    }

    @Test
    void crear_precioMenorOIgualACero_retorna400() throws Exception {
        ProductoRequest request = new ProductoRequest("DET-001", "Detergente 5L", BigDecimal.ZERO, 12);

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].campo").value("precio"));
    }

    @Test
    void crear_codigoDuplicado_retorna409() throws Exception {
        ProductoRequest request = new ProductoRequest("DET-001", "Detergente 5L", new BigDecimal("38500.00"), 12);
        when(productoService.crear(any(ProductoRequest.class)))
                .thenThrow(new CodigoDuplicadoException("producto", "DET-001"));

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Ya existe un producto con el código 'DET-001'"));
    }

    @Test
    void actualizar_datosValidos_retorna200() throws Exception {
        ProductoUpdateRequest request = new ProductoUpdateRequest("Detergente Actualizado", new BigDecimal("42000.00"), 20);
        when(productoService.actualizar(eq(1L), any(ProductoUpdateRequest.class))).thenReturn(
                new ProductoResponse(1L, "DET-001", "Detergente Actualizado", new BigDecimal("42000.00"), 20, true, false, LocalDateTime.now())
        );

        mockMvc.perform(put("/api/productos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Detergente Actualizado"))
                .andExpect(jsonPath("$.existencias").value(20));
    }

    @Test
    void cambiarEstadoActivo_retorna200() throws Exception {
        ProductoActivoRequest request = new ProductoActivoRequest(false);
        when(productoService.cambiarEstadoActivo(eq(1L), any(ProductoActivoRequest.class))).thenReturn(
                new ProductoResponse(1L, "DET-001", "Detergente 5L", new BigDecimal("38500.00"), 12, false, false, LocalDateTime.now())
        );

        mockMvc.perform(patch("/api/productos/1/activo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
    }
}
