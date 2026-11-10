package com.ladorada.sistema.inventario.controller;

import com.ladorada.sistema.catalogo.dto.ProductoResponse;
import com.ladorada.sistema.inventario.dto.AjusteInventarioRequest;
import com.ladorada.sistema.inventario.dto.MovimientoResponse;
import com.ladorada.sistema.inventario.model.TipoMovimiento;
import com.ladorada.sistema.inventario.service.InventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
@CrossOrigin(originPatterns = "*")
@Tag(name = "Inventario", description = "Operaciones de consulta de movimientos de inventario y monitoreo de stock bajo")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping("/movimientos")
    @Operation(summary = "Consultar movimientos de inventario", description = "Retorna el historial de movimientos de inventario ordenados descendentemente por fecha, con filtros opcionales por productoId, pedidoId y tipo.")
    public ResponseEntity<List<MovimientoResponse>> listarMovimientos(
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) Long pedidoId,
            @RequestParam(required = false) TipoMovimiento tipo
    ) {
        return ResponseEntity.ok(inventarioService.listarMovimientos(productoId, pedidoId, tipo));
    }

    @GetMapping("/stock-bajo")
    @Operation(summary = "Listar productos con stock bajo", description = "Retorna los productos activos cuyas existencias sean menores o iguales al umbral configurado (incluye los agotados).")
    public ResponseEntity<List<ProductoResponse>> listarStockBajo() {
        return ResponseEntity.ok(inventarioService.listarStockBajo());
    }

    @PostMapping("/ajuste")
    @Operation(summary = "Registrar ajuste de existencias (ingreso de mercancía)", description = "Incrementa las existencias de un producto y registra un movimiento de auditoría de tipo AJUSTE.")
    public ResponseEntity<ProductoResponse> ajustarStock(@Valid @RequestBody AjusteInventarioRequest request) {
        return ResponseEntity.ok(inventarioService.ajustarStock(request));
    }
}
