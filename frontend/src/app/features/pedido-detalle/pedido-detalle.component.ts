import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PedidosService } from '../../core/services/pedidos.service';
import { CatalogoService } from '../../core/services/catalogo.service';
import { AuthService } from '../../core/services/auth.service';
import {
  PedidoDetalle,
  EstadoPedido,
  Producto,
  DetalleStock,
  ApiErrorResponse,
  ActualizarPedidoRequest
} from '../../shared/models';

interface LineaEdicionItem {
  productoId: number;
  cantidad: number;
  precioUnitario: number;
  nombre?: string;
  codigo?: string;
}

@Component({
  selector: 'app-pedido-detalle',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './pedido-detalle.component.html',
  styleUrl: './pedido-detalle.component.scss'
})
export class PedidoDetalleComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly pedidosService = inject(PedidosService);
  private readonly catalogoService = inject(CatalogoService);
  private readonly authService = inject(AuthService);

  puedeConfirmarOModificar(): boolean {
    return this.authService.hasRole(['ADMINISTRADOR', 'VENDEDOR']);
  }

  puedeDespachar(): boolean {
    return this.authService.hasRole(['ADMINISTRADOR', 'BODEGA']);
  }

  puedeCancelarConfirmado(): boolean {
    return this.authService.hasRole(['ADMINISTRADOR', 'BODEGA']);
  }

  pedidoId: number | null = null;
  pedido: PedidoDetalle | null = null;
  cargando = true;
  accionEnProgreso = false;

  // Mensajes y alertas
  errorMensaje: string | null = null;
  detallesStock: DetalleStock[] = [];
  mensajeExito: string | null = null;

  // Modo edición de líneas (solo para PENDIENTE)
  modoEdicionLineas = false;
  lineasEdicion: LineaEdicionItem[] = [];
  productosActivos: Producto[] = [];
  guardandoLineas = false;

  // Modal de confirmación para cambios de estado
  mostrarModalConfirmacion = false;
  accionPendiente: EstadoPedido | null = null;
  modalTitulo = '';
  modalMensaje = '';
  modalAdvertencia = '';
  modalBotonClase = 'btn--primary';

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.pedidoId = Number(idParam);
      this.cargarDetalle();
    } else {
      this.errorMensaje = 'No se especificó un ID de pedido válido.';
      this.cargando = false;
    }
  }

  cargarDetalle(): void {
    if (!this.pedidoId) return;

    this.cargando = true;
    this.errorMensaje = null;
    this.detallesStock = [];

    this.pedidosService.getPedidoById(this.pedidoId).subscribe({
      next: (data) => {
        this.pedido = data;
        this.cargando = false;
      },
      error: (err) => {
        console.error('Error al cargar detalle del pedido:', err);
        this.cargando = false;
        if (err.status === 404) {
          this.errorMensaje = `El pedido #${this.pedidoId} no fue encontrado en el sistema.`;
        } else {
          this.errorMensaje = 'No fue posible cargar el detalle del pedido. Verifique la conexión con el servidor.';
        }
      }
    });
  }

  // Flujo de transiciones de estado
  solicitarConfirmar(): void {
    this.abrirModalTransicion(
      'CONFIRMADO',
      'Confirmar Pedido',
      '¿Está seguro de que desea confirmar este pedido?',
      '',
      'btn--success'
    );
  }

  solicitarDespachar(): void {
    this.abrirModalTransicion(
      'DESPACHADO',
      'Despachar Pedido',
      '¿Desea marcar este pedido como Despachado?',
      '',
      'btn--primary'
    );
  }

  solicitarRechazar(): void {
    this.abrirModalTransicion(
      'RECHAZADO',
      'Rechazar Pedido',
      '¿Está seguro de que desea rechazar este pedido?',
      '',
      'btn--danger'
    );
  }

  solicitarCancelar(): void {
    this.abrirModalTransicion(
      'CANCELADO',
      'Cancelar Pedido',
      '¿Está seguro de que desea cancelar este pedido?',
      '',
      'btn--danger'
    );
  }

  abrirModalTransicion(
    estado: EstadoPedido,
    titulo: string,
    mensaje: string,
    advertencia: string,
    botonClase: string
  ): void {
    this.accionPendiente = estado;
    this.modalTitulo = titulo;
    this.modalMensaje = mensaje;
    this.modalAdvertencia = advertencia;
    this.modalBotonClase = botonClase;
    this.mostrarModalConfirmacion = true;
  }

  cerrarModalConfirmacion(): void {
    this.mostrarModalConfirmacion = false;
    this.accionPendiente = null;
  }

  ejecutarTransicion(): void {
    if (!this.pedidoId || !this.accionPendiente) return;

    this.accionEnProgreso = true;
    this.errorMensaje = null;
    this.detallesStock = [];
    this.mensajeExito = null;

    const nuevoEstado = this.accionPendiente;
    this.cerrarModalConfirmacion();

    this.pedidosService.cambiarEstado(this.pedidoId, { estado: nuevoEstado }).subscribe({
      next: (pedidoActualizado) => {
        this.pedido = pedidoActualizado;
        this.accionEnProgreso = false;
        this.mensajeExito = `El estado del pedido #${pedidoActualizado.id} ha cambiado exitosamente a ${nuevoEstado}.`;
      },
      error: (err) => {
        this.accionEnProgreso = false;
        console.error('Error al cambiar estado del pedido:', err);

        const errorResp: ApiErrorResponse = err?.error;
        if (err.status === 409 && errorResp?.detallesStock && errorResp.detallesStock.length > 0) {
          this.errorMensaje = errorResp.message || 'Conflicto de existencias: Stock insuficiente para confirmar el pedido.';
          this.detallesStock = errorResp.detallesStock;
        } else if (errorResp?.message) {
          this.errorMensaje = errorResp.message;
        } else {
          this.errorMensaje = 'Ocurrió un error al intentar cambiar el estado del pedido.';
        }
      }
    });
  }

  // Modo edición de líneas
  iniciarEdicionLineas(): void {
    if (!this.pedido || this.pedido.estado !== 'PENDIENTE') return;

    this.errorMensaje = null;
    this.detallesStock = [];
    this.mensajeExito = null;

    // Cargar catálogo de productos activos si no se ha cargado
    if (this.productosActivos.length === 0) {
      this.catalogoService.getProductos(undefined, true).subscribe({
        next: (prods) => {
          this.productosActivos = prods;
          this.prepararLineasEdicion();
        },
        error: (err) => console.error('Error al cargar catálogo para edición:', err)
      });
    } else {
      this.prepararLineasEdicion();
    }
  }

  prepararLineasEdicion(): void {
    if (!this.pedido) return;

    this.lineasEdicion = this.pedido.lineas.map(l => ({
      productoId: l.productoId,
      cantidad: l.cantidad,
      precioUnitario: l.precioUnitario || 0,
      nombre: l.productoNombre,
      codigo: l.productoCodigo
    }));
    this.modoEdicionLineas = true;
  }

  cancelarEdicionLineas(): void {
    this.modoEdicionLineas = false;
    this.lineasEdicion = [];
  }

  agregarLineaEdicion(): void {
    const primerProd = this.productosActivos.length > 0 ? this.productosActivos[0] : null;
    this.lineasEdicion.push({
      productoId: primerProd ? primerProd.id : 0,
      cantidad: 1,
      precioUnitario: primerProd ? primerProd.precio : 0,
      nombre: primerProd ? primerProd.nombre : '',
      codigo: primerProd ? primerProd.codigo : ''
    });
  }

  quitarLineaEdicion(index: number): void {
    if (this.lineasEdicion.length > 1) {
      this.lineasEdicion.splice(index, 1);
    }
  }

  onProductoEdicionChange(linea: LineaEdicionItem): void {
    const prod = this.productosActivos.find(p => p.id === Number(linea.productoId));
    if (prod) {
      linea.precioUnitario = prod.precio;
      linea.nombre = prod.nombre;
      linea.codigo = prod.codigo;
    }
  }

  calcularSubtotalEdicion(linea: LineaEdicionItem): number {
    return (linea.cantidad || 0) * (linea.precioUnitario || 0);
  }

  calcularTotalEdicion(): number {
    return this.lineasEdicion.reduce((sum, l) => sum + this.calcularSubtotalEdicion(l), 0);
  }

  guardarCambiosLineas(): void {
    if (!this.pedidoId) return;

    this.errorMensaje = null;
    this.detallesStock = [];

    // Validar líneas
    for (let i = 0; i < this.lineasEdicion.length; i++) {
      const l = this.lineasEdicion[i];
      if (!l.productoId) {
        this.errorMensaje = `La línea ${i + 1} no tiene un producto seleccionado.`;
        return;
      }
      if (!l.cantidad || l.cantidad <= 0) {
        this.errorMensaje = `La línea ${i + 1} debe tener una cantidad entera mayor que cero.`;
        return;
      }
    }

    const payload: ActualizarPedidoRequest = {
      lineas: this.lineasEdicion.map(l => ({
        productoId: Number(l.productoId),
        cantidad: Math.floor(l.cantidad)
      }))
    };

    this.guardandoLineas = true;
    this.pedidosService.actualizarPedido(this.pedidoId, payload).subscribe({
      next: (pedidoActualizado) => {
        this.pedido = pedidoActualizado;
        this.guardandoLineas = false;
        this.modoEdicionLineas = false;
        this.mensajeExito = 'Las líneas del pedido han sido actualizadas y el total recalculado correctamente.';
      },
      error: (err) => {
        this.guardandoLineas = false;
        console.error('Error al actualizar líneas del pedido:', err);
        const errorResp: ApiErrorResponse = err?.error;
        if (errorResp?.message) {
          this.errorMensaje = errorResp.message;
        } else {
          this.errorMensaje = 'Ocurrió un error al actualizar las líneas del pedido.';
        }
      }
    });
  }

  // Helpers para Stepper / Timeline
  esPasoCompletado(paso: 'PENDIENTE' | 'CONFIRMADO' | 'DESPACHADO'): boolean {
    if (!this.pedido) return false;
    const estado = this.pedido.estado;

    if (paso === 'PENDIENTE') {
      return estado === 'CONFIRMADO' || estado === 'DESPACHADO';
    }
    if (paso === 'CONFIRMADO') {
      return estado === 'DESPACHADO';
    }
    return false;
  }

  esPasoActivo(paso: 'PENDIENTE' | 'CONFIRMADO' | 'DESPACHADO'): boolean {
    if (!this.pedido) return false;
    return this.pedido.estado === paso;
  }

  esEstadoTerminal(): boolean {
    if (!this.pedido) return false;
    return this.pedido.estado === 'DESPACHADO' ||
           this.pedido.estado === 'RECHAZADO' ||
           this.pedido.estado === 'CANCELADO';
  }
}
