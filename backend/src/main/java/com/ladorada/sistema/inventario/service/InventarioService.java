package com.ladorada.sistema.inventario.service;

import com.ladorada.sistema.catalogo.dto.ProductoResponse;
import com.ladorada.sistema.catalogo.model.Producto;
import com.ladorada.sistema.catalogo.repository.ProductoRepository;
import com.ladorada.sistema.common.exception.DetalleStockInsuficiente;
import com.ladorada.sistema.common.exception.RecursoNoEncontradoException;
import com.ladorada.sistema.common.exception.StockInsuficienteException;
import com.ladorada.sistema.inventario.dto.MovimientoResponse;
import com.ladorada.sistema.inventario.model.MovimientoInventario;
import com.ladorada.sistema.inventario.model.TipoMovimiento;
import com.ladorada.sistema.inventario.repository.MovimientoInventarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class InventarioService {

    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final int umbralStockBajo;

    public InventarioService(
            ProductoRepository productoRepository,
            MovimientoInventarioRepository movimientoInventarioRepository,
            @Value("${ladorada.inventario.umbral-stock-bajo:10}") int umbralStockBajo
    ) {
        this.productoRepository = productoRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.umbralStockBajo = umbralStockBajo;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void descontar(Long pedidoId, List<LineaInventario> lineas) {
        if (lineas == null || lineas.isEmpty()) {
            return;
        }

        for (LineaInventario linea : lineas) {
            if (linea.cantidad() <= 0) {
                throw new IllegalArgumentException(String.format(
                        "La cantidad a descontar para el producto ID %d debe ser mayor a cero (recibido: %d)",
                        linea.productoId(), linea.cantidad()
                ));
            }
        }

        // Ordenar por productoId para evitar deadlocks en bloqueos concurrentes
        List<LineaInventario> lineasOrdenadas = lineas.stream()
                .sorted(Comparator.comparing(LineaInventario::productoId))
                .toList();

        List<Producto> productosBloqueados = new ArrayList<>();
        List<DetalleStockInsuficiente> productosInsuficientes = new ArrayList<>();

        for (LineaInventario linea : lineasOrdenadas) {
            Producto producto = productoRepository.findByIdConBloqueo(linea.productoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto", linea.productoId()));

            productosBloqueados.add(producto);

            if (producto.getExistencias() < linea.cantidad()) {
                productosInsuficientes.add(new DetalleStockInsuficiente(
                        producto.getId(),
                        producto.getNombre(),
                        linea.cantidad(),
                        producto.getExistencias()
                ));
            }
        }

        // Si algún producto no tiene stock suficiente, abortar sin modificar nada
        if (!productosInsuficientes.isEmpty()) {
            throw new StockInsuficienteException(productosInsuficientes);
        }

        // Si todo alcanza, aplicar el descuento y registrar movimientos
        for (int i = 0; i < lineasOrdenadas.size(); i++) {
            LineaInventario linea = lineasOrdenadas.get(i);
            Producto producto = productosBloqueados.get(i);

            int nuevoStock = producto.getExistencias() - linea.cantidad();
            producto.setExistencias(nuevoStock);
            productoRepository.save(producto);

            MovimientoInventario movimiento = new MovimientoInventario(
                    producto.getId(),
                    pedidoId,
                    TipoMovimiento.DESCUENTO,
                    linea.cantidad(),
                    LocalDateTime.now()
            );
            movimientoInventarioRepository.save(movimiento);
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void restituir(Long pedidoId, List<LineaInventario> lineas) {
        if (lineas == null || lineas.isEmpty()) {
            return;
        }

        for (LineaInventario linea : lineas) {
            if (linea.cantidad() <= 0) {
                throw new IllegalArgumentException(String.format(
                        "La cantidad a restituir para el producto ID %d debe ser mayor a cero (recibido: %d)",
                        linea.productoId(), linea.cantidad()
                ));
            }
        }

        // Ordenar por productoId para evitar deadlocks
        List<LineaInventario> lineasOrdenadas = lineas.stream()
                .sorted(Comparator.comparing(LineaInventario::productoId))
                .toList();

        for (LineaInventario linea : lineasOrdenadas) {
            Producto producto = productoRepository.findByIdConBloqueo(linea.productoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto", linea.productoId()));

            producto.setExistencias(producto.getExistencias() + linea.cantidad());
            productoRepository.save(producto);

            MovimientoInventario movimiento = new MovimientoInventario(
                    producto.getId(),
                    pedidoId,
                    TipoMovimiento.RESTITUCION,
                    linea.cantidad(),
                    LocalDateTime.now()
            );
            movimientoInventarioRepository.save(movimiento);
        }
    }

    @Transactional(readOnly = true)
    public List<MovimientoResponse> listarMovimientos(Long productoId, Long pedidoId, TipoMovimiento tipo) {
        List<MovimientoInventario> movimientos = movimientoInventarioRepository.buscarMovimientos(productoId, pedidoId, tipo);
        return movimientos.stream()
                .map(MovimientoResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarStockBajo() {
        List<Producto> productos = productoRepository.findStockBajo(umbralStockBajo);
        return productos.stream()
                .map(p -> ProductoResponse.fromEntity(p, umbralStockBajo))
                .toList();
    }

    public int getUmbralStockBajo() {
        return umbralStockBajo;
    }
}
