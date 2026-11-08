package com.ladorada.sistema.pedidos.service;

import com.ladorada.sistema.catalogo.model.Cliente;
import com.ladorada.sistema.catalogo.model.Producto;
import com.ladorada.sistema.catalogo.repository.ClienteRepository;
import com.ladorada.sistema.catalogo.repository.ProductoRepository;
import com.ladorada.sistema.common.exception.PedidoNoEditableException;
import com.ladorada.sistema.common.exception.RecursoNoEncontradoException;
import com.ladorada.sistema.common.exception.TransicionNoPermitidaException;
import com.ladorada.sistema.inventario.service.InventarioService;
import com.ladorada.sistema.inventario.service.LineaInventario;
import com.ladorada.sistema.pedidos.dto.ActualizarPedidoRequest;
import com.ladorada.sistema.pedidos.dto.CrearPedidoRequest;
import com.ladorada.sistema.pedidos.dto.LineaPedidoRequest;
import com.ladorada.sistema.pedidos.dto.PedidoDetalleResponse;
import com.ladorada.sistema.pedidos.dto.PedidoResumenResponse;
import com.ladorada.sistema.pedidos.model.EstadoPedido;
import com.ladorada.sistema.pedidos.model.Pedido;
import com.ladorada.sistema.pedidos.model.PedidoHistorialEstado;
import com.ladorada.sistema.pedidos.model.PedidoLinea;
import com.ladorada.sistema.pedidos.repository.PedidoHistorialEstadoRepository;
import com.ladorada.sistema.pedidos.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PedidoHistorialEstadoRepository historialRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final InventarioService inventarioService;

    public PedidoService(
            PedidoRepository pedidoRepository,
            PedidoHistorialEstadoRepository historialRepository,
            ClienteRepository clienteRepository,
            ProductoRepository productoRepository,
            InventarioService inventarioService
    ) {
        this.pedidoRepository = pedidoRepository;
        this.historialRepository = historialRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
        this.inventarioService = inventarioService;
    }

    public PedidoDetalleResponse crear(CrearPedidoRequest request) {
        if (request.clienteId() == null) {
            throw new IllegalArgumentException("El clienteId es obligatorio");
        }
        if (request.lineas() == null || request.lineas().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe tener al menos una línea");
        }

        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", request.clienteId()));

        Pedido pedido = new Pedido(cliente);
        boolean confirmar = request.debeConfirmar();
        pedido.setEstado(confirmar ? EstadoPedido.CONFIRMADO : EstadoPedido.PENDIENTE);
        pedido.setStockDescontado(confirmar);

        List<PedidoLinea> lineasProcesadas = procesarYConstruirLineas(request.lineas(), pedido);
        BigDecimal total = calcularTotal(lineasProcesadas);
        pedido.setTotal(total);

        // Guardar y vaciar para obtener ID generado
        Pedido pedidoGuardado = pedidoRepository.saveAndFlush(pedido);

        // Registro de historial inicial: null -> PENDIENTE o null -> CONFIRMADO
        registrarHistorial(pedidoGuardado, null, pedidoGuardado.getEstado());

        // Si se solicitó confirmación inmediata, descontar del inventario
        if (confirmar) {
            List<LineaInventario> lineasInventario = extraerLineasInventario(pedidoGuardado);
            inventarioService.descontar(pedidoGuardado.getId(), lineasInventario);
        }

        List<PedidoHistorialEstado> historial = historialRepository.findByPedidoIdOrderByFechaAsc(pedidoGuardado.getId());
        return PedidoDetalleResponse.fromEntity(pedidoGuardado, historial);
    }

    @Transactional(readOnly = true)
    public List<PedidoResumenResponse> listar(EstadoPedido estado, Long clienteId) {
        List<Pedido> pedidos = pedidoRepository.buscarPedidos(estado, clienteId);
        return pedidos.stream()
                .map(PedidoResumenResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoDetalleResponse obtenerPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido", id));

        List<PedidoHistorialEstado> historial = historialRepository.findByPedidoIdOrderByFechaAsc(id);
        return PedidoDetalleResponse.fromEntity(pedido, historial);
    }

    public PedidoDetalleResponse actualizar(Long id, ActualizarPedidoRequest request) {
        if (request.lineas() == null || request.lineas().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe tener al menos una línea");
        }

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido", id));

        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new PedidoNoEditableException(id, pedido.getEstado().name());
        }

        pedido.limpiarLineas();
        List<PedidoLinea> nuevasLineas = procesarYConstruirLineas(request.lineas(), pedido);
        BigDecimal total = calcularTotal(nuevasLineas);
        pedido.setTotal(total);

        Pedido actualizado = pedidoRepository.save(pedido);
        List<PedidoHistorialEstado> historial = historialRepository.findByPedidoIdOrderByFechaAsc(id);
        return PedidoDetalleResponse.fromEntity(actualizado, historial);
    }

    public PedidoDetalleResponse cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("El nuevo estado es obligatorio");
        }

        // Bloqueo pesimista de escritura para evitar condiciones de carrera en confirmaciones simultáneas
        Pedido pedido = pedidoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido", id));

        EstadoPedido estadoActual = pedido.getEstado();

        // Si ya está en el estado solicitado, operación idempotente sin re-descuentos
        if (estadoActual == nuevoEstado) {
            List<PedidoHistorialEstado> historial = historialRepository.findByPedidoIdOrderByFechaAsc(id);
            return PedidoDetalleResponse.fromEntity(pedido, historial);
        }

        if (!estadoActual.puedeTransicionarA(nuevoEstado)) {
            throw new TransicionNoPermitidaException(estadoActual.name(), nuevoEstado.name());
        }

        switch (nuevoEstado) {
            case CONFIRMADO -> ejecutarConfirmacion(pedido, estadoActual);
            case CANCELADO -> ejecutarCancelacion(pedido, estadoActual);
            case RECHAZADO -> ejecutarRechazo(pedido, estadoActual);
            case DESPACHADO -> ejecutarDespacho(pedido, estadoActual);
            default -> throw new TransicionNoPermitidaException(estadoActual.name(), nuevoEstado.name());
        }

        Pedido pedidoFinal = pedidoRepository.save(pedido);
        List<PedidoHistorialEstado> historial = historialRepository.findByPedidoIdOrderByFechaAsc(id);
        return PedidoDetalleResponse.fromEntity(pedidoFinal, historial);
    }

    private void ejecutarConfirmacion(Pedido pedido, EstadoPedido estadoAnterior) {
        if (!pedido.isStockDescontado()) {
            List<LineaInventario> lineasInventario = extraerLineasInventario(pedido);
            inventarioService.descontar(pedido.getId(), lineasInventario);
            pedido.setStockDescontado(true);
        }
        pedido.setEstado(EstadoPedido.CONFIRMADO);
        registrarHistorial(pedido, estadoAnterior, EstadoPedido.CONFIRMADO);
    }

    private void ejecutarCancelacion(Pedido pedido, EstadoPedido estadoAnterior) {
        if (estadoAnterior == EstadoPedido.CONFIRMADO && pedido.isStockDescontado()) {
            List<LineaInventario> lineasInventario = extraerLineasInventario(pedido);
            inventarioService.restituir(pedido.getId(), lineasInventario);
            pedido.setStockDescontado(false);
        }
        // Si cancela desde PENDIENTE, no se toca el stock
        pedido.setEstado(EstadoPedido.CANCELADO);
        registrarHistorial(pedido, estadoAnterior, EstadoPedido.CANCELADO);
    }

    private void ejecutarRechazo(Pedido pedido, EstadoPedido estadoAnterior) {
        // Desde PENDIENTE, no toca el stock
        pedido.setEstado(EstadoPedido.RECHAZADO);
        registrarHistorial(pedido, estadoAnterior, EstadoPedido.RECHAZADO);
    }

    private void ejecutarDespacho(Pedido pedido, EstadoPedido estadoAnterior) {
        // Desde CONFIRMADO, el stock ya se descontó previamente
        pedido.setEstado(EstadoPedido.DESPACHADO);
        registrarHistorial(pedido, estadoAnterior, EstadoPedido.DESPACHADO);
    }

    private List<PedidoLinea> procesarYConstruirLineas(List<LineaPedidoRequest> lineasRequest, Pedido pedido) {
        // Consolidar productos repetidos sumando cantidades
        Map<Long, Integer> cantidadesConsolidadas = new LinkedHashMap<>();
        for (LineaPedidoRequest req : lineasRequest) {
            if (req.productoId() == null) {
                throw new IllegalArgumentException("El ID del producto es obligatorio");
            }
            if (req.cantidad() == null || req.cantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser un número entero mayor que cero.");
            }
            cantidadesConsolidadas.merge(req.productoId(), req.cantidad(), Integer::sum);
        }

        List<PedidoLinea> resultado = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : cantidadesConsolidadas.entrySet()) {
            Long productoId = entry.getKey();
            int cantidad = entry.getValue();

            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto", productoId));

            if (!producto.isActivo()) {
                throw new IllegalArgumentException(String.format("El producto '%s' no está activo para nuevos pedidos", producto.getNombre()));
            }

            PedidoLinea linea = new PedidoLinea(producto, cantidad, producto.getPrecio());
            pedido.agregarLinea(linea);
            resultado.add(linea);
        }

        return resultado;
    }

    private BigDecimal calcularTotal(List<PedidoLinea> lineas) {
        return lineas.stream()
                .map(PedidoLinea::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<LineaInventario> extraerLineasInventario(Pedido pedido) {
        return pedido.getLineas().stream()
                .map(l -> new LineaInventario(l.getProducto().getId(), l.getCantidad()))
                .toList();
    }

    private void registrarHistorial(Pedido pedido, EstadoPedido anterior, EstadoPedido nuevo) {
        PedidoHistorialEstado historial = new PedidoHistorialEstado(pedido, anterior, nuevo);
        historialRepository.save(historial);
    }
}
