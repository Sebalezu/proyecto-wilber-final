package com.ladorada.sistema.catalogo.controller;

import com.ladorada.sistema.catalogo.dto.ProductoActivoRequest;
import com.ladorada.sistema.catalogo.dto.ProductoRequest;
import com.ladorada.sistema.catalogo.dto.ProductoResponse;
import com.ladorada.sistema.catalogo.dto.ProductoUpdateRequest;
import com.ladorada.sistema.catalogo.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(originPatterns = "*")
@Tag(name = "Productos", description = "Operaciones de catálogo de productos de aseo y desinfección")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @Operation(summary = "Listar productos", description = "Retorna el listado de productos con filtros opcionales de búsqueda por texto (nombre o código) y estado activo.")
    public ResponseEntity<List<ProductoResponse>> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean activo
    ) {
        return ResponseEntity.ok(productoService.listar(q, activo));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID", description = "Busca y retorna la información detallada de un producto por su ID.")
    public ResponseEntity<ProductoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear producto", description = "Registra un nuevo producto. Si no se envía código, se genera automáticamente con formato ASE-XXX.")
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        ProductoResponse creado = productoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto", description = "Actualiza nombre, precio y existencias. Si cambian las existencias, se genera automáticamente un movimiento AJUSTE en el inventario.")
    public ResponseEntity<ProductoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoUpdateRequest request
    ) {
        ProductoResponse actualizado = productoService.actualizar(id, request);
        return ResponseEntity.ok(actualizado);
    }

    @PatchMapping("/{id}/activo")
    @Operation(summary = "Activar o desactivar producto", description = "Modifica el estado lógico activo del producto. Los productos nunca se eliminan físicamente.")
    public ResponseEntity<ProductoResponse> cambiarEstadoActivo(
            @PathVariable Long id,
            @Valid @RequestBody ProductoActivoRequest request
    ) {
        ProductoResponse actualizado = productoService.cambiarEstadoActivo(id, request);
        return ResponseEntity.ok(actualizado);
    }
}
