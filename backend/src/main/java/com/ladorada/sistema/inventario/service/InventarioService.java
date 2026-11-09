package com.ladorada.sistema.inventario.service;

import com.ladorada.sistema.catalogo.dto.ProductoResponse;
import com.ladorada.sistema.catalogo.model.Producto;
import com.ladorada.sistema.catalogo.repository.ProductoRepository;
import com.ladorada.sistema.common.exception.DetalleStockInsuficiente;
import com.ladorada.sistema.common.exception.RecursoNoEncontradoException;
import com.ladorada.sistema.common.exception.StockInsuficienteException;
import com.ladorada.sistema.inventario.dto.AjusteInventarioRequest;
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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

        // Agrupar líneas por productoId sumando cantidades y ordenando de forma ascendente para evitar deadlocks
        Map<Long, Integer> cantidadesAgrupadas = new TreeMap<>();
        for (LineaInventario linea : lineas) {
            cantidadesAgrupadas.merge(linea.productoId(), linea.cantidad(), Integer::sum);
        }

        // FASE 1: Bloquear cada producto con bloqueo pesimista y validar TODO el stock sin modificar nada
        List<Producto> productosBloqueados = new ArrayList<>();
        List<Integer> cantidadesList = new ArrayList<>();
        List<DetalleStockInsuficiente> productosInsuficientes = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : cantidadesAgrupadas.entrySet()) {
            Long productoId = entry.getKey();
            int cantidadTotal = entry.getValue();

            Producto producto = productoRepository.findByIdConBloqueo(productoId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto", productoId));

            productosBloqueados.add(producto);
            cantidadesList.add(cantidadTotal);

            if (producto.getExistencias() < cantidadTotal) {
                productosInsuficientes.add(new DetalleStockInsuficiente(
                        producto.getId(),
                        producto.getNombre(),
                        cantidadTotal,
                        producto.getExistencias()
                ));
            }
        }

        // Si algún producto no alcanza, abortar lanzando excepción con todos los detalles sin modificar entidades
        if (!productosInsuficientes.isEmpty()) {
            throw new StockInsuficienteException(productosInsuficientes);
        }

        // FASE 2: Solo si todo alcanza, aplicar los descuentos y registrar un movimiento DESCUENTO por producto
        for (int i = 0; i < productosBloqueados.size(); i++) {
            Producto producto = productosBloqueados.get(i);
            int cantidad = cantidadesList.get(i);

            int nuevoStock = producto.getExistencias() - cantidad;
            producto.setExistencias(nuevoStock);
            productoRepository.save(producto);

            MovimientoInventario movimiento = new MovimientoInventario(
                    producto.getId(),
                    pedidoId,
                    TipoMovimiento.DESCUENTO,
                    cantidad,
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

        // Agrupar líneas por productoId sumando cantidades y ordenando de forma ascendente para evitar deadlocks
        Map<Long, Integer> cantidadesAgrupadas = new TreeMap<>();
        for (LineaInventario linea : lineas) {
            cantidadesAgrupadas.merge(linea.productoId(), linea.cantidad(), Integer::sum);
        }

        for (Map.Entry<Long, Integer> entry : cantidadesAgrupadas.entrySet()) {
            Long productoId = entry.getKey();
            int cantidadTotal = entry.getValue();

            Producto producto = productoRepository.findByIdConBloqueo(productoId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto", productoId));

            producto.setExistencias(producto.getExistencias() + cantidadTotal);
            productoRepository.save(producto);

            MovimientoInventario movimiento = new MovimientoInventario(
                    producto.getId(),
                    pedidoId,
                    TipoMovimiento.RESTITUCION,
                    cantidadTotal,
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

    public ProductoResponse ajustarStock(AjusteInventarioRequest request) {
        if (request.productoId() == null) {
            throw new IllegalArgumentException("El ID del producto es obligatorio");
        }
        if (request.cantidad() == null || request.cantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }

        Producto producto = productoRepository.findByIdConBloqueo(request.productoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", request.productoId()));

        int nuevoStock = producto.getExistencias() + request.cantidad();
        producto.setExistencias(nuevoStock);
        Producto guardado = productoRepository.save(producto);

        MovimientoInventario movimiento = new MovimientoInventario(
                producto.getId(),
                null,
                TipoMovimiento.AJUSTE,
                request.cantidad(),
                LocalDateTime.now()
        );
        movimientoInventarioRepository.save(movimiento);

        return ProductoResponse.fromEntity(guardado, umbralStockBajo);
    }

    public int getUmbralStockBajo() {
        return umbralStockBajo;
    }
}
