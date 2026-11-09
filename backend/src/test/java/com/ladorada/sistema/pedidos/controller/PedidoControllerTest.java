package com.ladorada.sistema.pedidos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ladorada.sistema.common.exception.DetalleStockInsuficiente;
import com.ladorada.sistema.common.exception.GlobalExceptionHandler;
import com.ladorada.sistema.common.exception.PedidoNoEditableException;
import com.ladorada.sistema.common.exception.RecursoNoEncontradoException;
import com.ladorada.sistema.common.exception.StockInsuficienteException;
import com.ladorada.sistema.common.exception.TransicionNoPermitidaException;
import com.ladorada.sistema.pedidos.dto.ActualizarPedidoRequest;
import com.ladorada.sistema.pedidos.dto.CambiarEstadoPedidoRequest;
import com.ladorada.sistema.pedidos.dto.ClienteResumenDto;
import com.ladorada.sistema.pedidos.dto.CrearPedidoRequest;
import com.ladorada.sistema.pedidos.dto.HistorialEstadoResponse;
import com.ladorada.sistema.pedidos.dto.LineaPedidoRequest;
import com.ladorada.sistema.pedidos.dto.PedidoDetalleResponse;
import com.ladorada.sistema.pedidos.dto.PedidoLineaResponse;
import com.ladorada.sistema.pedidos.dto.PedidoResumenResponse;
import com.ladorada.sistema.pedidos.model.EstadoPedido;
import com.ladorada.sistema.pedidos.service.PedidoService;
import org.junit.jupiter.api.DisplayName;
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

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PedidoController.class)
@Import(GlobalExceptionHandler.class)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PedidoService pedidoService;

    private PedidoDetalleResponse crearPedidoDetalleEjemplo(Long id, EstadoPedido estado, boolean stockDescontado) {
        ClienteResumenDto cliente = new ClienteResumenDto(1L, "Distribuidora La 14", "3001234567");
        PedidoLineaResponse linea = new PedidoLineaResponse(
                1L,
                10L,
                "Cloro 2 L",
                "CLO-001",
                2,
                new BigDecimal("15000.00"),
                new BigDecimal("30000.00")
        );
        HistorialEstadoResponse historial = new HistorialEstadoResponse(1L, null, estado, LocalDateTime.now());

        return new PedidoDetalleResponse(
                id,
                cliente,
                List.of(linea),
                new BigDecimal("30000.00"),
                estado,
                stockDescontado,
                LocalDateTime.now(),
                List.of(historial)
        );
    }

    @Test
    @DisplayName("crear_exitoso_retorna201")
    void crear_exitoso_retorna201() throws Exception {
        CrearPedidoRequest request = new CrearPedidoRequest(
                1L,
                false,
                List.of(new LineaPedidoRequest(10L, 2))
        );

        PedidoDetalleResponse response = crearPedidoDetalleEjemplo(100L, EstadoPedido.PENDIENTE, false);
        when(pedidoService.crear(any(CrearPedidoRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.total").value(30000.00))
                .andExpect(jsonPath("$.cliente.nombre").value("Distribuidora La 14"))
                .andExpect(jsonPath("$.lineas", hasSize(1)));
    }

    @Test
    @DisplayName("crear_sinClienteId_retorna400")
    void crear_sinClienteId_retorna400() throws Exception {
        CrearPedidoRequest request = new CrearPedidoRequest(
                null,
                false,
                List.of(new LineaPedidoRequest(10L, 2))
        );

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[?(@.campo == 'clienteId')].mensaje")
                        .value(hasItem("El clienteId es obligatorio")));
    }

    @Test
    @DisplayName("crear_lineasVacias_retorna400")
    void crear_lineasVacias_retorna400() throws Exception {
        CrearPedidoRequest request = new CrearPedidoRequest(
                1L,
                false,
                List.of()
        );

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[?(@.campo == 'lineas')].mensaje")
                        .value(hasItem("El pedido debe tener al menos una línea")));
    }

    @Test
    @DisplayName("crear_cantidadMenorOIgualACero_retorna400ConMensajeExacto")
    void crear_cantidadMenorOIgualACero_retorna400ConMensajeExacto() throws Exception {
        CrearPedidoRequest request = new CrearPedidoRequest(
                1L,
                false,
                List.of(new LineaPedidoRequest(10L, 0))
        );

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].mensaje").value("La cantidad debe ser un número entero mayor que cero."));
    }

    @Test
    @DisplayName("crear_clienteOProductoInexistente_retorna404")
    void crear_clienteOProductoInexistente_retorna404() throws Exception {
        CrearPedidoRequest request = new CrearPedidoRequest(
                999L,
                false,
                List.of(new LineaPedidoRequest(10L, 2))
        );

        when(pedidoService.crear(any(CrearPedidoRequest.class)))
                .thenThrow(new RecursoNoEncontradoException("Cliente", 999L));

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Cliente con ID 999 no encontrado"));
    }

    @Test
    @DisplayName("crear_conConfirmarTrue_sinStock_retorna409ConDetalleStockInsuficiente")
    void crear_conConfirmarTrue_sinStock_retorna409ConDetalleStockInsuficiente() throws Exception {
        CrearPedidoRequest request = new CrearPedidoRequest(
                1L,
                true,
                List.of(new LineaPedidoRequest(10L, 100))
        );

        StockInsuficienteException ex = new StockInsuficienteException(
                List.of(new DetalleStockInsuficiente(10L, "Cloro 2 L", 100, 50))
        );

        when(pedidoService.crear(any(CrearPedidoRequest.class))).thenThrow(ex);

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detallesStock", hasSize(1)))
                .andExpect(jsonPath("$.detallesStock[0].nombre").value("Cloro 2 L"))
                .andExpect(jsonPath("$.detallesStock[0].solicitado").value(100))
                .andExpect(jsonPath("$.detallesStock[0].disponible").value(50));
    }

    @Test
    @DisplayName("listar_retorna200YListaResumen")
    void listar_retorna200YListaResumen() throws Exception {
        ClienteResumenDto cliente = new ClienteResumenDto(1L, "Distribuidora La 14", "3001234567");
        PedidoResumenResponse resumen = new PedidoResumenResponse(
                100L,
                cliente,
                new BigDecimal("30000.00"),
                EstadoPedido.PENDIENTE,
                false,
                LocalDateTime.now()
        );

        when(pedidoService.listar(null, null)).thenReturn(List.of(resumen));

        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"))
                .andExpect(jsonPath("$[0].total").value(30000.00));
    }

    @Test
    @DisplayName("obtenerPorId_existente_retorna200")
    void obtenerPorId_existente_retorna200() throws Exception {
        PedidoDetalleResponse response = crearPedidoDetalleEjemplo(100L, EstadoPedido.PENDIENTE, false);
        when(pedidoService.obtenerPorId(100L)).thenReturn(response);

        mockMvc.perform(get("/api/pedidos/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.cliente.id").value(1))
                .andExpect(jsonPath("$.lineas", hasSize(1)))
                .andExpect(jsonPath("$.historial", hasSize(1)));
    }

    @Test
    @DisplayName("obtenerPorId_noExistente_retorna404")
    void obtenerPorId_noExistente_retorna404() throws Exception {
        when(pedidoService.obtenerPorId(999L)).thenThrow(new RecursoNoEncontradoException("Pedido", 999L));

        mockMvc.perform(get("/api/pedidos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Pedido con ID 999 no encontrado"));
    }

    @Test
    @DisplayName("actualizar_pedidoPendiente_retorna200")
    void actualizar_pedidoPendiente_retorna200() throws Exception {
        ActualizarPedidoRequest request = new ActualizarPedidoRequest(
                List.of(new LineaPedidoRequest(10L, 5))
        );

        PedidoDetalleResponse response = crearPedidoDetalleEjemplo(100L, EstadoPedido.PENDIENTE, false);
        when(pedidoService.actualizar(eq(100L), any(ActualizarPedidoRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/pedidos/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    @DisplayName("actualizar_pedidoNoPendiente_retorna409")
    void actualizar_pedidoNoPendiente_retorna409() throws Exception {
        ActualizarPedidoRequest request = new ActualizarPedidoRequest(
                List.of(new LineaPedidoRequest(10L, 5))
        );

        when(pedidoService.actualizar(eq(100L), any(ActualizarPedidoRequest.class)))
                .thenThrow(new PedidoNoEditableException(100L, "CONFIRMADO"));

        mockMvc.perform(put("/api/pedidos/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Solo se pueden editar pedidos en estado PENDIENTE (estado actual: CONFIRMADO)"));
    }

    @Test
    @DisplayName("cambiarEstado_transicionValida_retorna200")
    void cambiarEstado_transicionValida_retorna200() throws Exception {
        CambiarEstadoPedidoRequest request = new CambiarEstadoPedidoRequest(EstadoPedido.CONFIRMADO);
        PedidoDetalleResponse response = crearPedidoDetalleEjemplo(100L, EstadoPedido.CONFIRMADO, true);

        when(pedidoService.cambiarEstado(100L, EstadoPedido.CONFIRMADO)).thenReturn(response);

        mockMvc.perform(patch("/api/pedidos/100/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"))
                .andExpect(jsonPath("$.stockDescontado").value(true));
    }

    @Test
    @DisplayName("cambiarEstado_transicionInvalida_retorna409")
    void cambiarEstado_transicionInvalida_retorna409() throws Exception {
        CambiarEstadoPedidoRequest request = new CambiarEstadoPedidoRequest(EstadoPedido.PENDIENTE);

        when(pedidoService.cambiarEstado(100L, EstadoPedido.PENDIENTE))
                .thenThrow(new TransicionNoPermitidaException("DESPACHADO", "PENDIENTE"));

        mockMvc.perform(patch("/api/pedidos/100/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Transición no permitida: DESPACHADO → PENDIENTE"));
    }

    @Test
    @DisplayName("cambiarEstado_stockInsuficiente_retorna409")
    void cambiarEstado_stockInsuficiente_retorna409() throws Exception {
        CambiarEstadoPedidoRequest request = new CambiarEstadoPedidoRequest(EstadoPedido.CONFIRMADO);

        StockInsuficienteException ex = new StockInsuficienteException(
                List.of(new DetalleStockInsuficiente(10L, "Cloro 2 L", 10, 2))
        );

        when(pedidoService.cambiarEstado(100L, EstadoPedido.CONFIRMADO)).thenThrow(ex);

        mockMvc.perform(patch("/api/pedidos/100/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detallesStock", hasSize(1)))
                .andExpect(jsonPath("$.detallesStock[0].nombre").value("Cloro 2 L"));
    }
}
