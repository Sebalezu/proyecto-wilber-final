package com.ladorada.sistema.inventario.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ladorada.sistema.catalogo.dto.ProductoResponse;
import com.ladorada.sistema.common.exception.GlobalExceptionHandler;
import com.ladorada.sistema.inventario.dto.AjusteInventarioRequest;
import com.ladorada.sistema.inventario.dto.MovimientoResponse;
import com.ladorada.sistema.inventario.model.TipoMovimiento;
import com.ladorada.sistema.inventario.service.InventarioService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventarioController.class)
@Import(GlobalExceptionHandler.class)
class InventarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventarioService inventarioService;

    @Test
    void listarMovimientos_retorna200YLista() throws Exception {
        when(inventarioService.listarMovimientos(null, null, null)).thenReturn(List.of(
                new MovimientoResponse(1L, 1L, 100L, TipoMovimiento.DESCUENTO, 3, LocalDateTime.now())
        ));

        mockMvc.perform(get("/api/inventario/movimientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].tipo").value("DESCUENTO"))
                .andExpect(jsonPath("$[0].cantidad").value(3));
    }

    @Test
    void listarStockBajo_retorna200YLista() throws Exception {
        when(inventarioService.listarStockBajo()).thenReturn(List.of(
                new ProductoResponse(2L, "CLO-002", "Cloro", new BigDecimal("16800.00"), 4, true, true, LocalDateTime.now())
        ));

        mockMvc.perform(get("/api/inventario/stock-bajo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("CLO-002"))
                .andExpect(jsonPath("$[0].stockBajo").value(true));
    }

    @Test
    void ajustarStock_retorna200YProductoActualizado() throws Exception {
        ProductoResponse response = new ProductoResponse(1L, "DET-001", "Detergente 5L", new BigDecimal("38500.00"), 25, true, false, LocalDateTime.now());
        when(inventarioService.ajustarStock(any(AjusteInventarioRequest.class))).thenReturn(response);

        AjusteInventarioRequest request = new AjusteInventarioRequest(1L, 10, "Ingreso a bodega / Ajuste físico");

        mockMvc.perform(post("/api/inventario/ajuste")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.existencias").value(25))
                .andExpect(jsonPath("$.codigo").value("DET-001"));
    }
}
