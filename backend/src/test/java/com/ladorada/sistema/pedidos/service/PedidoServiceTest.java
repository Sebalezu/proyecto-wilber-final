package com.ladorada.sistema.pedidos.service;

import com.ladorada.sistema.catalogo.model.Cliente;
import com.ladorada.sistema.catalogo.model.Producto;
import com.ladorada.sistema.catalogo.repository.ClienteRepository;
import com.ladorada.sistema.catalogo.repository.ProductoRepository;
import com.ladorada.sistema.common.exception.DetalleStockInsuficiente;
import com.ladorada.sistema.common.exception.PedidoNoEditableException;
import com.ladorada.sistema.common.exception.RecursoNoEncontradoException;
import com.ladorada.sistema.common.exception.StockInsuficienteException;
import com.ladorada.sistema.common.exception.TransicionNoPermitidaException;
import com.ladorada.sistema.inventario.service.InventarioService;
import com.ladorada.sistema.inventario.service.LineaInventario;
import com.ladorada.sistema.pedidos.dto.ActualizarPedidoRequest;
import com.ladorada.sistema.pedidos.dto.CrearPedidoRequest;
import com.ladorada.sistema.pedidos.dto.LineaPedidoRequest;
import com.ladorada.sistema.pedidos.dto.PedidoDetalleResponse;
import com.ladorada.sistema.pedidos.model.EstadoPedido;
import com.ladorada.sistema.pedidos.model.Pedido;
import com.ladorada.sistema.pedidos.model.PedidoHistorialEstado;
import com.ladorada.sistema.pedidos.model.PedidoLinea;
import com.ladorada.sistema.pedidos.repository.PedidoHistorialEstadoRepository;
import com.ladorada.sistema.pedidos.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private PedidoHistorialEstadoRepository historialRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private InventarioService inventarioService;

    private PedidoService pedidoService;

    private Cliente clienteEjemplo;
    private Producto productoCloro;
    private Producto productoDetergente;
    private Producto productoInactivo;

    @BeforeEach
    void setUp() {
        pedidoService = new PedidoService(
                pedidoRepository,
                historialRepository,
                clienteRepository,
                productoRepository,
                inventarioService
        );

        clienteEjemplo = new Cliente(1L, "Distribuidora La 14", "3001234567");

        productoCloro = new Producto(
                10L,
                "CLO-001",
                "Cloro 2 L",
                new BigDecimal("15000.00"),
                50,
                true,
                LocalDateTime.now()
        );

        productoDetergente = new Producto(
                20L,
                "DET-001",
                "Detergente 5L",
                new BigDecimal("40000.00"),
                30,
                true,
                LocalDateTime.now()
        );

        productoInactivo = new Producto(
                30L,
                "DES-001",
                "Desinfectante Inactivo",
                new BigDecimal("25000.00"),
                10,
                false,
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("1. crearPedido_variasLineas_totalCorrecto: varias líneas, calcula bien el total (suma de cantidad * precioUnitario)")
    void crearPedido_variasLineas_totalCorrecto() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteEjemplo));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoCloro));
        when(productoRepository.findById(20L)).thenReturn(Optional.of(productoDetergente));

        when(pedidoRepository.saveAndFlush(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido p = invocation.getArgument(0);
            p.setId(100L);
            return p;
        });

        CrearPedidoRequest request = new CrearPedidoRequest(
                1L,
                false,
                List.of(
                        new LineaPedidoRequest(10L, 2), // 2 * 15000 = 30000
                        new LineaPedidoRequest(20L, 3)  // 3 * 40000 = 120000
                )
        );

        PedidoDetalleResponse response = pedidoService.crear(request);

        assertNotNull(response);
        assertEquals(EstadoPedido.PENDIENTE, response.estado());
        // 30,000 + 120,000 = 150,000
        assertEquals(new BigDecimal("150000.00"), response.total());
        assertEquals(2, response.lineas().size());
        assertFalse(response.stockDescontado());
        verify(inventarioService, never()).descontar(any(), any());
    }

    @Test
    @DisplayName("2. crearPedido_lineasRepetidas_seSumanEnUnaSola: si vienen dos líneas con el mismo productoId, se consolidan en una sola sumando las cantidades")
    void crearPedido_lineasRepetidas_seSumanEnUnaSola() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteEjemplo));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoCloro));

        when(pedidoRepository.saveAndFlush(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido p = invocation.getArgument(0);
            p.setId(101L);
            return p;
        });

        CrearPedidoRequest request = new CrearPedidoRequest(
                1L,
                false,
                List.of(
                        new LineaPedidoRequest(10L, 2),
                        new LineaPedidoRequest(10L, 5) // Mismo producto: total cantidad = 7
                )
        );

        PedidoDetalleResponse response = pedidoService.crear(request);

        assertEquals(1, response.lineas().size());
        assertEquals(7, response.lineas().get(0).cantidad());
        // 7 * 15,000 = 105,000
        assertEquals(new BigDecimal("105000.00"), response.total());
        assertEquals(new BigDecimal("105000.00"), response.lineas().get(0).subtotal());
    }

    @Test
    @DisplayName("3. crearPedido_cantidadMenorOIgualACero_lanzaErrorExacto: si alguna cantidad es <= 0, debe lanzar excepción / responder 400 con el mensaje exacto")
    void crearPedido_cantidadMenorOIgualACero_lanzaErrorExacto() {
        CrearPedidoRequest requestCero = new CrearPedidoRequest(
                1L,
                false,
                List.of(new LineaPedidoRequest(10L, 0))
        );

        IllegalArgumentException exCero = assertThrows(
                IllegalArgumentException.class,
                () -> pedidoService.crear(requestCero)
        );
        assertEquals("La cantidad debe ser un número entero mayor que cero.", exCero.getMessage());

        CrearPedidoRequest requestNegativo = new CrearPedidoRequest(
                1L,
                false,
                List.of(new LineaPedidoRequest(10L, -3))
        );

        IllegalArgumentException exNegativo = assertThrows(
                IllegalArgumentException.class,
                () -> pedidoService.crear(requestNegativo)
        );
        assertEquals("La cantidad debe ser un número entero mayor que cero.", exNegativo.getMessage());
    }

    @Test
    @DisplayName("4. crearPedido_productoInexistente_lanza404: RecursoNoEncontradoException")
    void crearPedido_productoInexistente_lanza404() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteEjemplo));
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        CrearPedidoRequest request = new CrearPedidoRequest(
                1L,
                false,
                List.of(new LineaPedidoRequest(999L, 2))
        );

        RecursoNoEncontradoException ex = assertThrows(
                RecursoNoEncontradoException.class,
                () -> pedidoService.crear(request)
        );
        assertTrue(ex.getMessage().contains("Producto"));
        assertTrue(ex.getMessage().contains("999"));
    }

    @Test
    @DisplayName("5. crearPedido_productoInactivo_lanzaError: no se pueden pedir productos inactivos")
    void crearPedido_productoInactivo_lanzaError() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteEjemplo));
        when(productoRepository.findById(30L)).thenReturn(Optional.of(productoInactivo));

        CrearPedidoRequest request = new CrearPedidoRequest(
                1L,
                false,
                List.of(new LineaPedidoRequest(30L, 1))
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> pedidoService.crear(request)
        );
        assertTrue(ex.getMessage().contains("no está activo"));
    }

    @Test
    @DisplayName("6. crearPedido_clienteInexistente_lanza404: RecursoNoEncontradoException")
    void crearPedido_clienteInexistente_lanza404() {
        when(clienteRepository.findById(888L)).thenReturn(Optional.empty());

        CrearPedidoRequest request = new CrearPedidoRequest(
                888L,
                false,
                List.of(new LineaPedidoRequest(10L, 1))
        );

        RecursoNoEncontradoException ex = assertThrows(
                RecursoNoEncontradoException.class,
                () -> pedidoService.crear(request)
        );
        assertTrue(ex.getMessage().contains("Cliente"));
        assertTrue(ex.getMessage().contains("888"));
    }

    @Test
    @DisplayName("7. crearPedido_conConfirmarTrue_stockSuficiente_quedaConfirmadoYDescuentaStock")
    void crearPedido_conConfirmarTrue_stockSuficiente_quedaConfirmadoYDescuentaStock() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteEjemplo));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoCloro));

        when(pedidoRepository.saveAndFlush(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido p = invocation.getArgument(0);
            p.setId(102L);
            return p;
        });

        CrearPedidoRequest request = new CrearPedidoRequest(
                1L,
                true, // confirmar = true
                List.of(new LineaPedidoRequest(10L, 4))
        );

        PedidoDetalleResponse response = pedidoService.crear(request);

        assertEquals(EstadoPedido.CONFIRMADO, response.estado());
        assertTrue(response.stockDescontado());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<LineaInventario>> captor = ArgumentCaptor.forClass(List.class);
        verify(inventarioService, times(1)).descontar(eq(102L), captor.capture());

        List<LineaInventario> lineasInventario = captor.getValue();
        assertEquals(1, lineasInventario.size());
        assertEquals(10L, lineasInventario.get(0).productoId());
        assertEquals(4, lineasInventario.get(0).cantidad());
    }

    @Test
    @DisplayName("8. crearPedido_conConfirmarTrue_sinStock_lanzaStockInsuficienteException")
    void crearPedido_conConfirmarTrue_sinStock_lanzaStockInsuficienteException() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteEjemplo));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoCloro));

        when(pedidoRepository.saveAndFlush(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido p = invocation.getArgument(0);
            p.setId(103L);
            return p;
        });

        doThrow(new StockInsuficienteException(
                List.of(new DetalleStockInsuficiente(10L, "Cloro 2 L", 60, 50))
        )).when(inventarioService).descontar(eq(103L), anyList());

        CrearPedidoRequest request = new CrearPedidoRequest(
                1L,
                true,
                List.of(new LineaPedidoRequest(10L, 60))
        );

        StockInsuficienteException ex = assertThrows(
                StockInsuficienteException.class,
                () -> pedidoService.crear(request)
        );

        assertEquals(1, ex.getDetalles().size());
        assertEquals("Cloro 2 L", ex.getDetalles().get(0).nombre());
    }

    @Test
    @DisplayName("9. confirmar_dosVeces_noVuelveADescontar: si ya tiene stockDescontado=true, no se vuelve a descontar")
    void confirmar_dosVeces_noVuelveADescontar() {
        Pedido pedido = new Pedido(clienteEjemplo);
        pedido.setId(104L);
        pedido.setEstado(EstadoPedido.CONFIRMADO);
        pedido.setStockDescontado(true);
        pedido.agregarLinea(new PedidoLinea(productoCloro, 3, productoCloro.getPrecio()));

        when(pedidoRepository.findByIdForUpdate(104L)).thenReturn(Optional.of(pedido));

        // Intento de volver a transicionar a CONFIRMADO
        PedidoDetalleResponse response = pedidoService.cambiarEstado(104L, EstadoPedido.CONFIRMADO);

        assertEquals(EstadoPedido.CONFIRMADO, response.estado());
        assertTrue(response.stockDescontado());
        verify(inventarioService, never()).descontar(any(), any());
    }

    @Test
    @DisplayName("10. cancelar_pedidoConfirmado_llamaARestituirUnaVez: restituye líneas y stockDescontado pasa a false")
    void cancelar_pedidoConfirmado_llamaARestituirUnaVez() {
        Pedido pedido = new Pedido(clienteEjemplo);
        pedido.setId(105L);
        pedido.setEstado(EstadoPedido.CONFIRMADO);
        pedido.setStockDescontado(true);
        pedido.agregarLinea(new PedidoLinea(productoCloro, 5, productoCloro.getPrecio()));

        when(pedidoRepository.findByIdForUpdate(105L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PedidoDetalleResponse response = pedidoService.cambiarEstado(105L, EstadoPedido.CANCELADO);

        assertEquals(EstadoPedido.CANCELADO, response.estado());
        assertFalse(response.stockDescontado());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<LineaInventario>> captor = ArgumentCaptor.forClass(List.class);
        verify(inventarioService, times(1)).restituir(eq(105L), captor.capture());

        assertEquals(1, captor.getValue().size());
        assertEquals(5, captor.getValue().get(0).cantidad());
    }

    @Test
    @DisplayName("11. cancelar_o_rechazar_pedidoPendiente_noTocaStock: no llama a descontar ni a restituir")
    void cancelar_o_rechazar_pedidoPendiente_noTocaStock() {
        // Caso 1: Cancelar desde PENDIENTE
        Pedido pedidoPendiente1 = new Pedido(clienteEjemplo);
        pedidoPendiente1.setId(106L);
        pedidoPendiente1.setEstado(EstadoPedido.PENDIENTE);
        pedidoPendiente1.setStockDescontado(false);
        pedidoPendiente1.agregarLinea(new PedidoLinea(productoCloro, 2, productoCloro.getPrecio()));

        when(pedidoRepository.findByIdForUpdate(106L)).thenReturn(Optional.of(pedidoPendiente1));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PedidoDetalleResponse responseCancel = pedidoService.cambiarEstado(106L, EstadoPedido.CANCELADO);
        assertEquals(EstadoPedido.CANCELADO, responseCancel.estado());
        assertFalse(responseCancel.stockDescontado());

        // Caso 2: Rechazar desde PENDIENTE
        Pedido pedidoPendiente2 = new Pedido(clienteEjemplo);
        pedidoPendiente2.setId(107L);
        pedidoPendiente2.setEstado(EstadoPedido.PENDIENTE);
        pedidoPendiente2.setStockDescontado(false);
        pedidoPendiente2.agregarLinea(new PedidoLinea(productoCloro, 2, productoCloro.getPrecio()));

        when(pedidoRepository.findByIdForUpdate(107L)).thenReturn(Optional.of(pedidoPendiente2));

        PedidoDetalleResponse responseRechazar = pedidoService.cambiarEstado(107L, EstadoPedido.RECHAZADO);
        assertEquals(EstadoPedido.RECHAZADO, responseRechazar.estado());
        assertFalse(responseRechazar.stockDescontado());

        verify(inventarioService, never()).descontar(any(), any());
        verify(inventarioService, never()).restituir(any(), any());
    }

    @Test
    @DisplayName("12. cambiarEstado_transicionInvalida_lanzaTransicionNoPermitidaException")
    void cambiarEstado_transicionInvalida_lanzaTransicionNoPermitidaException() {
        Pedido pedidoDespachado = new Pedido(clienteEjemplo);
        pedidoDespachado.setId(108L);
        pedidoDespachado.setEstado(EstadoPedido.DESPACHADO);

        when(pedidoRepository.findByIdForUpdate(108L)).thenReturn(Optional.of(pedidoDespachado));

        // DESPACHADO -> PENDIENTE prohibida
        TransicionNoPermitidaException ex = assertThrows(
                TransicionNoPermitidaException.class,
                () -> pedidoService.cambiarEstado(108L, EstadoPedido.PENDIENTE)
        );
        assertEquals("Transición no permitida: DESPACHADO → PENDIENTE", ex.getMessage());

        // CANCELADO -> CONFIRMADO prohibida
        Pedido pedidoCancelado = new Pedido(clienteEjemplo);
        pedidoCancelado.setId(109L);
        pedidoCancelado.setEstado(EstadoPedido.CANCELADO);
        when(pedidoRepository.findByIdForUpdate(109L)).thenReturn(Optional.of(pedidoCancelado));

        TransicionNoPermitidaException ex2 = assertThrows(
                TransicionNoPermitidaException.class,
                () -> pedidoService.cambiarEstado(109L, EstadoPedido.CONFIRMADO)
        );
        assertEquals("Transición no permitida: CANCELADO → CONFIRMADO", ex2.getMessage());
    }

    @Test
    @DisplayName("13. actualizar_pedidoNoPendiente_lanzaPedidoNoEditableException")
    void actualizar_pedidoNoPendiente_lanzaPedidoNoEditableException() {
        Pedido pedidoConfirmado = new Pedido(clienteEjemplo);
        pedidoConfirmado.setId(110L);
        pedidoConfirmado.setEstado(EstadoPedido.CONFIRMADO);

        when(pedidoRepository.findById(110L)).thenReturn(Optional.of(pedidoConfirmado));

        ActualizarPedidoRequest request = new ActualizarPedidoRequest(
                List.of(new LineaPedidoRequest(10L, 5))
        );

        PedidoNoEditableException ex = assertThrows(
                PedidoNoEditableException.class,
                () -> pedidoService.actualizar(110L, request)
        );
        assertTrue(ex.getMessage().contains("Solo se pueden editar pedidos en estado PENDIENTE"));
        assertTrue(ex.getMessage().contains("CONFIRMADO"));
    }

    @Test
    @DisplayName("14. cadaCambioDeEstado_agregaRegistroAlHistorial: verificar que creación y cambios de estado guardan historial")
    void cadaCambioDeEstado_agregaRegistroAlHistorial() {
        // Paso 1: Creación
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteEjemplo));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoCloro));
        when(pedidoRepository.saveAndFlush(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido p = invocation.getArgument(0);
            p.setId(111L);
            return p;
        });

        CrearPedidoRequest reqCrear = new CrearPedidoRequest(1L, false, List.of(new LineaPedidoRequest(10L, 2)));
        pedidoService.crear(reqCrear);

        ArgumentCaptor<PedidoHistorialEstado> captorHistorial = ArgumentCaptor.forClass(PedidoHistorialEstado.class);
        verify(historialRepository, times(1)).save(captorHistorial.capture());

        PedidoHistorialEstado hCreacion = captorHistorial.getValue();
        assertNull(hCreacion.getEstadoAnterior());
        assertEquals(EstadoPedido.PENDIENTE, hCreacion.getEstadoNuevo());

        // Paso 2: Transición PENDIENTE -> CONFIRMADO
        Pedido pedido = new Pedido(clienteEjemplo);
        pedido.setId(111L);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.agregarLinea(new PedidoLinea(productoCloro, 2, productoCloro.getPrecio()));

        when(pedidoRepository.findByIdForUpdate(111L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        pedidoService.cambiarEstado(111L, EstadoPedido.CONFIRMADO);

        verify(historialRepository, times(2)).save(captorHistorial.capture());
        PedidoHistorialEstado hConfirmar = captorHistorial.getValue();
        assertEquals(EstadoPedido.PENDIENTE, hConfirmar.getEstadoAnterior());
        assertEquals(EstadoPedido.CONFIRMADO, hConfirmar.getEstadoNuevo());
    }

    @Test
    @DisplayName("actualizar_pedidoPendiente_recalculaTotalYActualizaLineas")
    void actualizar_pedidoPendiente_recalculaTotalYActualizaLineas() {
        Pedido pedido = new Pedido(clienteEjemplo);
        pedido.setId(112L);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.agregarLinea(new PedidoLinea(productoCloro, 1, productoCloro.getPrecio()));
        pedido.setTotal(new BigDecimal("15000.00"));

        when(pedidoRepository.findById(112L)).thenReturn(Optional.of(pedido));
        when(productoRepository.findById(20L)).thenReturn(Optional.of(productoDetergente));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ActualizarPedidoRequest request = new ActualizarPedidoRequest(
                List.of(new LineaPedidoRequest(20L, 2)) // 2 * 40,000 = 80,000
        );

        PedidoDetalleResponse response = pedidoService.actualizar(112L, request);

        assertEquals(1, response.lineas().size());
        assertEquals("DET-001", response.lineas().get(0).productoCodigo());
        assertEquals(new BigDecimal("80000.00"), response.total());
    }
}

