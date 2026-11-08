package com.ladorada.sistema.catalogo.service;

import com.ladorada.sistema.catalogo.dto.ProductoActivoRequest;
import com.ladorada.sistema.catalogo.dto.ProductoRequest;
import com.ladorada.sistema.catalogo.dto.ProductoResponse;
import com.ladorada.sistema.catalogo.dto.ProductoUpdateRequest;
import com.ladorada.sistema.catalogo.model.Producto;
import com.ladorada.sistema.catalogo.repository.ProductoRepository;
import com.ladorada.sistema.common.exception.CodigoDuplicadoException;
import com.ladorada.sistema.common.exception.RecursoNoEncontradoException;
import com.ladorada.sistema.inventario.model.MovimientoInventario;
import com.ladorada.sistema.inventario.model.TipoMovimiento;
import com.ladorada.sistema.inventario.repository.MovimientoInventarioRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private MovimientoInventarioRepository movimientoInventarioRepository;

    private ProductoService productoService;

    private Producto productoExistente;

    @BeforeEach
    void setUp() {
        productoService = new ProductoService(productoRepository, movimientoInventarioRepository, 10);
        productoExistente = new Producto(
                1L,
                "DET-001",
                "Detergente Líquido Industrial 5L",
                new BigDecimal("38500.00"),
                12,
                true,
                LocalDateTime.now()
        );
    }

    @Test
    void crear_conCodigoExplicitoValido_guardaProducto() {
        ProductoRequest request = new ProductoRequest("JAB-100", "Jabón Especial", new BigDecimal("15000.00"), 20);
        when(productoRepository.existsByCodigo("JAB-100")).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> {
            Producto p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        ProductoResponse creado = productoService.crear(request);

        assertNotNull(creado);
        assertEquals("JAB-100", creado.codigo());
        assertEquals("Jabón Especial", creado.nombre());
        assertFalse(creado.stockBajo());
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    void crear_sinCodigo_generaConsecutivoAutomatico() {
        ProductoRequest request = new ProductoRequest(null, "Desinfectante Pino", new BigDecimal("12000.00"), 5);
        when(productoRepository.findCodigosByPrefijo("ASE-")).thenReturn(List.of("ASE-001", "ASE-002"));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> {
            Producto p = inv.getArgument(0);
            p.setId(11L);
            return p;
        });

        ProductoResponse creado = productoService.crear(request);

        assertNotNull(creado);
        assertEquals("ASE-003", creado.codigo());
        assertTrue(creado.stockBajo()); // 5 <= 10
    }

    @Test
    void crear_codigoDuplicado_lanzaCodigoDuplicadoException() {
        ProductoRequest request = new ProductoRequest("DET-001", "Otro Detergente", new BigDecimal("20000.00"), 10);
        when(productoRepository.existsByCodigo("DET-001")).thenReturn(true);

        assertThrows(CodigoDuplicadoException.class, () -> productoService.crear(request));
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void obtenerPorId_existente_calculaStockBajo() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoExistente));

        ProductoResponse response = productoService.obtenerPorId(1L);

        assertEquals("DET-001", response.codigo());
        assertFalse(response.stockBajo()); // existencias 12 > umbral 10
    }

    @Test
    void obtenerPorId_stockBajoTrue_siExistenciasMenorOIgualUmbral() {
        productoExistente.setExistencias(8);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoExistente));

        ProductoResponse response = productoService.obtenerPorId(1L);

        assertTrue(response.stockBajo()); // 8 <= 10
    }

    @Test
    void obtenerPorId_inexistente_lanzaRecursoNoEncontradoException() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> productoService.obtenerPorId(99L));
    }

    @Test
    void actualizar_existenciasCambiadas_registraMovimientoAjuste() {
        ProductoUpdateRequest request = new ProductoUpdateRequest(
                "Detergente Modificado",
                new BigDecimal("40000.00"),
                18 // anterior era 12 -> diferencia +6
        );
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoExistente));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoResponse response = productoService.actualizar(1L, request);

        assertEquals("Detergente Modificado", response.nombre());
        assertEquals(18, response.existencias());

        ArgumentCaptor<MovimientoInventario> captor = ArgumentCaptor.forClass(MovimientoInventario.class);
        verify(movimientoInventarioRepository).save(captor.capture());
        MovimientoInventario mov = captor.getValue();
        assertEquals(1L, mov.getProductoId());
        assertEquals(TipoMovimiento.AJUSTE, mov.getTipo());
        assertEquals(6, mov.getCantidad()); // |18 - 12| = 6
    }

    @Test
    void actualizar_existenciasSinCambio_noRegistraMovimientoAjuste() {
        ProductoUpdateRequest request = new ProductoUpdateRequest(
                "Detergente Modificado",
                new BigDecimal("40000.00"),
                12 // misma cantidad
        );
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoExistente));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        productoService.actualizar(1L, request);

        verify(movimientoInventarioRepository, never()).save(any(MovimientoInventario.class));
    }

    @Test
    void cambiarEstadoActivo_modificaEstadoSinBorrar() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoExistente));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoResponse response = productoService.cambiarEstadoActivo(1L, new ProductoActivoRequest(false));

        assertFalse(response.activo());
        verify(productoRepository).save(productoExistente);
    }
}
