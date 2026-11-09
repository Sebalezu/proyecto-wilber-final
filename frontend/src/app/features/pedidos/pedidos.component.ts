import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { PedidosService } from '../../core/services/pedidos.service';
import { CatalogoService } from '../../core/services/catalogo.service';
import { PedidoResumen, EstadoPedido, Cliente } from '../../shared/models';

@Component({
  selector: 'app-pedidos',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './pedidos.component.html',
  styleUrl: './pedidos.component.scss'
})
export class PedidosComponent implements OnInit {
  private readonly pedidosService = inject(PedidosService);
  private readonly catalogoService = inject(CatalogoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  pedidos: PedidoResumen[] = [];
  clientes: Cliente[] = [];

  cargando = false;
  errorMensaje: string | null = null;

  // Filtros
  estadoSeleccionado: string = 'TODOS';
  clienteSeleccionadoId: number | null = null;
  filtroTextoCliente: string = '';

  estadosDisponibles: { valor: string; etiqueta: string }[] = [
    { valor: 'TODOS', etiqueta: 'Todos' },
    { valor: 'PENDIENTE', etiqueta: 'Pendientes' },
    { valor: 'CONFIRMADO', etiqueta: 'Confirmados' },
    { valor: 'DESPACHADO', etiqueta: 'Despachados' },
    { valor: 'RECHAZADO', etiqueta: 'Rechazados' },
    { valor: 'CANCELADO', etiqueta: 'Cancelados' }
  ];

  ngOnInit(): void {
    // Si viene filtro por query param (ej. desde Dashboard)
    this.route.queryParams.subscribe(params => {
      if (params['estado']) {
        this.estadoSeleccionado = params['estado'];
      }
      this.cargarPedidos();
    });

    this.cargarClientes();
  }

  cargarClientes(): void {
    this.catalogoService.getClientes().subscribe({
      next: (data) => this.clientes = data,
      error: (err) => console.error('Error al cargar clientes:', err)
    });
  }

  cargarPedidos(): void {
    this.cargando = true;
    this.errorMensaje = null;

    const estadoParam = this.estadoSeleccionado !== 'TODOS'
      ? (this.estadoSeleccionado as EstadoPedido)
      : undefined;

    const clienteIdParam = this.clienteSeleccionadoId ? Number(this.clienteSeleccionadoId) : undefined;

    this.pedidosService.getPedidos(estadoParam, clienteIdParam).subscribe({
      next: (data) => {
        this.pedidos = data;
        this.cargando = false;
      },
      error: (err) => {
        console.error('Error al cargar pedidos:', err);
        this.errorMensaje = 'No se pudieron cargar los pedidos. Intente nuevamente.';
        this.cargando = false;
      }
    });
  }

  filtrarPorEstado(estado: string): void {
    this.estadoSeleccionado = estado;
    this.cargarPedidos();
  }

  onClienteChange(): void {
    this.cargarPedidos();
  }

  limpiarFiltros(): void {
    this.estadoSeleccionado = 'TODOS';
    this.clienteSeleccionadoId = null;
    this.filtroTextoCliente = '';
    this.cargarPedidos();
  }

  verDetalle(pedidoId: number): void {
    this.router.navigate(['/pedidos', pedidoId]);
  }

  get pedidosFiltrados(): PedidoResumen[] {
    if (!this.filtroTextoCliente.trim()) {
      return this.pedidos;
    }
    const q = this.filtroTextoCliente.toLowerCase();
    return this.pedidos.filter(p =>
      p.cliente?.nombre?.toLowerCase().includes(q) ||
      p.id.toString().includes(q)
    );
  }
}
