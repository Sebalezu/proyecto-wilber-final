package com.ladorada.sistema.inventario.service;

import com.ladorada.sistema.catalogo.model.Producto;
import com.ladorada.sistema.catalogo.repository.ProductoRepository;
import com.ladorada.sistema.common.exception.RecursoNoEncontradoException;
import com.ladorada.sistema.common.exception.StockInsuficienteException;
import com.ladorada.sistema.inventario.model.MovimientoInventario;
import com.ladorada.sistema.inventario.model.TipoMovimiento;
import com.ladorada.sistema.inventario.repository.MovimientoInventarioRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private MovimientoInventarioRepository movimientoInventarioRepository;

    private InventarioService inventarioService;

    private Producto productoCloro;
    private Producto productoDetergente;

    @BeforeEach
    void setUp() {
        inventarioService = new InventarioService(productoRepository, movimientoInventarioRepository, 10);

        productoCloro = new Producto(
                1L,
                "CLO-002",
                "Cloro 2 L",
                new BigDecimal("16800.00"),
                8, // 8 disponibles
                true,
                LocalDateTime.now()
        );

        productoDetergente = new Producto(
                2L,
                "DET-001",
                "Detergente 5L",
                new BigDecimal("38500.00"),
                20, // 20 disponibles
                true,
                LocalDateTime.now()
        );
    }

    @Test
    void descontar_stockSuficiente_descuentaExistenciasYRegistraMovimientoDescuento() {
        when(productoRepository.findByIdConBloqueo(1L)).thenReturn(Optional.of(productoCloro));
        when(productoRepository.findByIdConBloqueo(2L)).thenReturn(Optional.of(productoDetergente));

        List<LineaInventario> lineas = List.of(
                new LineaInventario(2L, 5), // pediste 5, hay 20
                new LineaInventario(1L, 3)  // pediste 3, hay 8
        );

        inventarioService.descontar(100L, lineas);

        assertEquals(5, productoCloro.getExistencias()); // 8 - 3 = 5
        assertEquals(15, productoDetergente.getExistencias()); // 20 - 5 = 15

        verify(productoRepository).save(productoCloro);
        verify(productoRepository).save(productoDetergente);

        ArgumentCaptor<MovimientoInventario> captor = ArgumentCaptor.forClass(MovimientoInventario.class);
        verify(movimientoInventarioRepository, times(2)).save(captor.capture());

        List<MovimientoInventario> movimientos = captor.getAllValues();
        assertEquals(TipoMovimiento.DESCUENTO, movimientos.get(0).getTipo());
        assertEquals(100L, movimientos.get(0).getPedidoId());
    }

    @Test
    void descontar_stockInsuficiente_noModificaNadaYLanzaExcepcionConMensajeExacto() {
        when(productoRepository.findByIdConBloqueo(1L)).thenReturn(Optional.of(productoCloro));

        List<LineaInventario> lineas = List.of(
                new LineaInventario(1L, 10) // pediste 10 y hay 8 disponibles
        );

        StockInsuficienteException ex = assertThrows(
                StockInsuficienteException.class,
                () -> inventarioService.descontar(100L, lineas)
        );

        assertEquals("Cloro 2 L: pediste 10 y hay 8 disponibles", ex.getMessage());
        assertEquals(8, productoCloro.getExistencias()); // NO modificado
        verify(productoRepository, never()).save(any(Producto.class));
        verify(movimientoInventarioRepository, never()).save(any(MovimientoInventario.class));
    }

    @Test
    void descontar_cantidadMenorOIgualACero_lanzaIllegalArgumentException() {
        List<LineaInventario> lineas = List.of(
                new LineaInventario(1L, 0)
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> inventarioService.descontar(100L, lineas)
        );

        assertTrue(ex.getMessage().contains("mayor a cero"));
        verify(productoRepository, never()).findByIdConBloqueo(any());
    }

    @Test
    void descontar_productoInexistente_lanzaRecursoNoEncontradoException() {
        when(productoRepository.findByIdConBloqueo(99L)).thenReturn(Optional.empty());

        List<LineaInventario> lineas = List.of(
                new LineaInventario(99L, 2)
        );

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> inventarioService.descontar(100L, lineas)
        );
    }

    @Test
    void restituir_lineasValidas_sumaExistenciasYRegistraMovimientoRestitucion() {
        when(productoRepository.findByIdConBloqueo(1L)).thenReturn(Optional.of(productoCloro));

        List<LineaInventario> lineas = List.of(
                new LineaInventario(1L, 4)
        );

        inventarioService.restituir(100L, lineas);

        assertEquals(12, productoCloro.getExistencias()); // 8 + 4 = 12
        verify(productoRepository).save(productoCloro);

        ArgumentCaptor<MovimientoInventario> captor = ArgumentCaptor.forClass(MovimientoInventario.class);
        verify(movimientoInventarioRepository).save(captor.capture());
        MovimientoInventario mov = captor.getValue();
        assertEquals(TipoMovimiento.RESTITUCION, mov.getTipo());
        assertEquals(4, mov.getCantidad());
        assertEquals(100L, mov.getPedidoId());
    }

    @Test
    void restituir_cantidadNegativa_lanzaIllegalArgumentException() {
        List<LineaInventario> lineas = List.of(
                new LineaInventario(1L, -2)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> inventarioService.restituir(100L, lineas)
        );
    }

    @Test
    @DisplayName("(a) dos líneas del mismo producto cuya suma supera el stock -> error")
    void descontar_lineasRepetidasSuperanStock_lanzaStockInsuficienteException() {
        when(productoRepository.findByIdConBloqueo(1L)).thenReturn(Optional.of(productoCloro)); // 8 disponibles

        // Dos líneas del producto 1: 5 + 4 = 9 > 8
        List<LineaInventario> lineas = List.of(
                new LineaInventario(1L, 5),
                new LineaInventario(1L, 4)
        );

        StockInsuficienteException ex = assertThrows(
                StockInsuficienteException.class,
                () -> inventarioService.descontar(200L, lineas)
        );

        assertEquals("Cloro 2 L: pediste 9 y hay 8 disponibles", ex.getMessage());
        assertEquals(8, productoCloro.getExistencias()); // No se modificó
        verify(productoRepository, never()).save(any(Producto.class));
        verify(movimientoInventarioRepository, never()).save(any(MovimientoInventario.class));
    }

    @Test
    @DisplayName("(b) primer producto con stock y segundo sin stock -> verifica que NO se llamó a save ni se modificaron existencias del primero")
    void descontar_primerProductoConStockYSegundoSinStock_noModificaElPrimeroNiLlamaSave() {
        // Orden ascendente por productoId: 1L (Cloro, stock 8), 2L (Detergente, stock 20)
        // Para que el primero tenga stock y el segundo no:
        // Producto 1 (Cloro): pedimos 5 (hay 8 -> suficiente)
        // Producto 2 (Detergente): pedimos 25 (hay 20 -> insuficiente)
        when(productoRepository.findByIdConBloqueo(1L)).thenReturn(Optional.of(productoCloro));
        when(productoRepository.findByIdConBloqueo(2L)).thenReturn(Optional.of(productoDetergente));

        List<LineaInventario> lineas = List.of(
                new LineaInventario(1L, 5),
                new LineaInventario(2L, 25)
        );

        StockInsuficienteException ex = assertThrows(
                StockInsuficienteException.class,
                () -> inventarioService.descontar(300L, lineas)
        );

        assertTrue(ex.getMessage().contains("Detergente 5L: pediste 25 y hay 20 disponibles"));
        // El primer producto NO debe haber sido alterado
        assertEquals(8, productoCloro.getExistencias());
        assertEquals(20, productoDetergente.getExistencias());
        // NO se debe haber llamado a save para ningún producto ni movimiento
        verify(productoRepository, never()).save(any(Producto.class));
        verify(movimientoInventarioRepository, never()).save(any(MovimientoInventario.class));
    }

    @Test
    @DisplayName("(c) líneas repetidas válidas se agrupan en un solo movimiento")
    void descontar_lineasRepetidasValidas_seAgrupanEnUnSoloMovimientoYDescuento() {
        when(productoRepository.findByIdConBloqueo(1L)).thenReturn(Optional.of(productoCloro)); // 8 disponibles

        // Dos líneas del producto 1: 2 + 3 = 5 <= 8
        List<LineaInventario> lineas = List.of(
                new LineaInventario(1L, 2),
                new LineaInventario(1L, 3)
        );

        inventarioService.descontar(400L, lineas);

        assertEquals(3, productoCloro.getExistencias()); // 8 - 5 = 3
        verify(productoRepository, times(1)).save(productoCloro);

        ArgumentCaptor<MovimientoInventario> captor = ArgumentCaptor.forClass(MovimientoInventario.class);
        verify(movimientoInventarioRepository, times(1)).save(captor.capture());

        MovimientoInventario mov = captor.getValue();
        assertEquals(TipoMovimiento.DESCUENTO, mov.getTipo());
        assertEquals(5, mov.getCantidad()); // Cantidad consolidada
        assertEquals(1L, mov.getProductoId());
        assertEquals(400L, mov.getPedidoId());
    }

    @Test
    @DisplayName("(c-restituir) líneas repetidas válidas en restituir se agrupan en un solo movimiento")
    void restituir_lineasRepetidasValidas_seAgrupanEnUnSoloMovimiento() {
        when(productoRepository.findByIdConBloqueo(1L)).thenReturn(Optional.of(productoCloro)); // 8 disponibles

        List<LineaInventario> lineas = List.of(
                new LineaInventario(1L, 2),
                new LineaInventario(1L, 3)
        );

        inventarioService.restituir(500L, lineas);

        assertEquals(13, productoCloro.getExistencias()); // 8 + 5 = 13
        verify(productoRepository, times(1)).save(productoCloro);

        ArgumentCaptor<MovimientoInventario> captor = ArgumentCaptor.forClass(MovimientoInventario.class);
        verify(movimientoInventarioRepository, times(1)).save(captor.capture());

        MovimientoInventario mov = captor.getValue();
        assertEquals(TipoMovimiento.RESTITUCION, mov.getTipo());
        assertEquals(5, mov.getCantidad()); // Cantidad consolidada
        assertEquals(1L, mov.getProductoId());
        assertEquals(500L, mov.getPedidoId());
    }
}
