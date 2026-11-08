package com.ladorada.sistema.pedidos.controller;

import com.ladorada.sistema.pedidos.dto.ActualizarPedidoRequest;
import com.ladorada.sistema.pedidos.dto.CambiarEstadoPedidoRequest;
import com.ladorada.sistema.pedidos.dto.CrearPedidoRequest;
import com.ladorada.sistema.pedidos.dto.PedidoDetalleResponse;
import com.ladorada.sistema.pedidos.dto.PedidoResumenResponse;
import com.ladorada.sistema.pedidos.model.EstadoPedido;
import com.ladorada.sistema.pedidos.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Gestión del ciclo de vida y operaciones de pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    @Operation(summary = "Crear pedido", description = "Registra un nuevo pedido. Si confirmar es true, se descuenta inventario inmediatamente y nace en estado CONFIRMADO.")
    public ResponseEntity<PedidoDetalleResponse> crear(@Valid @RequestBody CrearPedidoRequest request) {
        PedidoDetalleResponse creado = pedidoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping
    @Operation(summary = "Listar pedidos", description = "Retorna el listado de pedidos en formato resumen con filtros opcionales por estado y cliente.")
    public ResponseEntity<List<PedidoResumenResponse>> listar(
            @RequestParam(required = false) EstadoPedido estado,
            @RequestParam(required = false) Long clienteId
    ) {
        return ResponseEntity.ok(pedidoService.listar(estado, clienteId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de pedido", description = "Retorna los datos completos de un pedido incluyendo cliente, líneas calculadas e historial cronológico de estados.")
    public ResponseEntity<PedidoDetalleResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar líneas de pedido", description = "Permite editar las líneas y cantidades de un pedido exclusivamente en estado PENDIENTE, recalculando su total.")
    public ResponseEntity<PedidoDetalleResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarPedidoRequest request
    ) {
        PedidoDetalleResponse actualizado = pedidoService.actualizar(id, request);
        return ResponseEntity.ok(actualizado);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado de pedido", description = "Transiciona el estado del pedido validando el flujo de estados y aplicando efectos de inventario (descuento o restitución).")
    public ResponseEntity<PedidoDetalleResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoPedidoRequest request
    ) {
        PedidoDetalleResponse actualizado = pedidoService.cambiarEstado(id, request.estado());
        return ResponseEntity.ok(actualizado);
    }
}
