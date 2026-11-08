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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ProductoService {

    private static final String PREFIJO_CONSECUTIVO = "ASE-";

    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final int umbralStockBajo;

    public ProductoService(
            ProductoRepository productoRepository,
            MovimientoInventarioRepository movimientoInventarioRepository,
            @Value("${ladorada.inventario.umbral-stock-bajo:10}") int umbralStockBajo
    ) {
        this.productoRepository = productoRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.umbralStockBajo = umbralStockBajo;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(String q, Boolean activo) {
        String query = (q != null && !q.trim().isEmpty()) ? q.trim() : null;
        List<Producto> productos = productoRepository.buscarProductos(query, activo);
        return productos.stream()
                .map(p -> ProductoResponse.fromEntity(p, umbralStockBajo))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", id));
        return ProductoResponse.fromEntity(producto, umbralStockBajo);
    }

    public ProductoResponse crear(ProductoRequest request) {
        String codigoFinal;
        if (request.codigo() != null && !request.codigo().trim().isEmpty()) {
            codigoFinal = request.codigo().trim();
            if (productoRepository.existsByCodigo(codigoFinal)) {
                throw new CodigoDuplicadoException("producto", codigoFinal);
            }
        } else {
            codigoFinal = generarSiguienteCodigoConsecutivo();
        }

        Producto nuevo = new Producto(
                codigoFinal,
                request.nombre().trim(),
                request.precio(),
                request.existencias(),
                true
        );

        Producto guardado = productoRepository.save(nuevo);
        return ProductoResponse.fromEntity(guardado, umbralStockBajo);
    }

    public ProductoResponse actualizar(Long id, ProductoUpdateRequest request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", id));

        int existenciasAnteriores = producto.getExistencias();
        int nuevasExistencias = request.existencias();
        int diferencia = nuevasExistencias - existenciasAnteriores;

        producto.setNombre(request.nombre().trim());
        producto.setPrecio(request.precio());
        producto.setExistencias(nuevasExistencias);

        if (diferencia != 0) {
            MovimientoInventario movimiento = new MovimientoInventario(
                    producto.getId(),
                    null,
                    TipoMovimiento.AJUSTE,
                    Math.abs(diferencia),
                    LocalDateTime.now()
            );
            movimientoInventarioRepository.save(movimiento);
        }

        Producto actualizado = productoRepository.save(producto);
        return ProductoResponse.fromEntity(actualizado, umbralStockBajo);
    }

    public ProductoResponse cambiarEstadoActivo(Long id, ProductoActivoRequest request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", id));

        producto.setActivo(request.activo());
        Producto actualizado = productoRepository.save(producto);
        return ProductoResponse.fromEntity(actualizado, umbralStockBajo);
    }

    public int getUmbralStockBajo() {
        return umbralStockBajo;
    }

    private synchronized String generarSiguienteCodigoConsecutivo() {
        List<String> codigosExistentes = productoRepository.findCodigosByPrefijo(PREFIJO_CONSECUTIVO);
        int maxConsecutivo = 0;

        for (String codigo : codigosExistentes) {
            if (codigo.startsWith(PREFIJO_CONSECUTIVO)) {
                String numeroStr = codigo.substring(PREFIJO_CONSECUTIVO.length());
                try {
                    int numero = Integer.parseInt(numeroStr);
                    if (numero > maxConsecutivo) {
                        maxConsecutivo = numero;
                    }
                } catch (NumberFormatException ignored) {
                    // Ignorar códigos que no tengan formato numérico estricto
                }
            }
        }

        int siguiente = maxConsecutivo + 1;
        return String.format("%s%03d", PREFIJO_CONSECUTIVO, siguiente);
    }
}
