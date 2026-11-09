import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CatalogoService } from '../../core/services/catalogo.service';
import { PedidosService } from '../../core/services/pedidos.service';
import { Cliente, Producto, CrearPedidoRequest, DetalleStock, ApiErrorResponse } from '../../shared/models';

interface LineaFormItem {
  productoId: number | null;
  cantidad: number;
  precioUnitario: number;
}

@Component({
  selector: 'app-nuevo-pedido',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './nuevo-pedido.component.html',
  styleUrl: './nuevo-pedido.component.scss'
})
export class NuevoPedidoComponent implements OnInit {
  private readonly catalogoService = inject(CatalogoService);
  private readonly pedidosService = inject(PedidosService);
  private readonly router = inject(Router);

  clientes: Cliente[] = [];
  productos: Producto[] = [];

  clienteSeleccionadoId: number | null = null;
  lineas: LineaFormItem[] = [];

  cargandoInicial = true;
  enviando = false;

  // Errores
  errorGeneral: string | null = null;
  detallesStock: DetalleStock[] = [];

  // Modal para agregar nuevo cliente rápido
  mostrarModalCliente = false;
  nuevoClienteNombre = '';
  nuevoClienteTelefono = '';
  guardandoCliente = false;
  errorClienteModal: string | null = null;

  ngOnInit(): void {
    this.cargarDatosIniciales();
  }

  cargarDatosIniciales(): void {
    this.cargandoInicial = true;

    // Cargar clientes
    this.catalogoService.getClientes().subscribe({
      next: (clientes) => {
        this.clientes = clientes;
        if (clientes.length > 0 && !this.clienteSeleccionadoId) {
          this.clienteSeleccionadoId = clientes[0].id;
        }
      },
      error: (err) => console.error('Error al cargar clientes:', err)
    });

    // Cargar productos activos
    this.catalogoService.getProductos(undefined, true).subscribe({
      next: (prods) => {
        this.productos = prods;
        this.cargandoInicial = false;
        // Iniciar con una línea por defecto
        if (this.lineas.length === 0) {
          this.agregarLinea();
        }
      },
      error: (err) => {
        console.error('Error al cargar productos:', err);
        this.cargandoInicial = false;
      }
    });
  }

  agregarLinea(): void {
    const primerProd = this.productos.length > 0 ? this.productos[0] : null;
    this.lineas.push({
      productoId: primerProd ? primerProd.id : null,
      cantidad: 1,
      precioUnitario: primerProd ? primerProd.precio : 0
    });
  }

  quitarLinea(index: number): void {
    if (this.lineas.length > 1) {
      this.lineas.splice(index, 1);
    }
  }

  onProductoChange(linea: LineaFormItem): void {
    const prod = this.productos.find(p => p.id === Number(linea.productoId));
    if (prod) {
      linea.precioUnitario = prod.precio;
    } else {
      linea.precioUnitario = 0;
    }
  }

  calcularSubtotal(linea: LineaFormItem): number {
    return (linea.cantidad || 0) * (linea.precioUnitario || 0);
  }

  calcularTotalPreview(): number {
    return this.lineas.reduce((total, linea) => total + this.calcularSubtotal(linea), 0);
  }

  guardarPedido(confirmar: boolean): void {
    this.errorGeneral = null;
    this.detallesStock = [];

    if (!this.clienteSeleccionadoId) {
      this.errorGeneral = 'Debe seleccionar un cliente.';
      return;
    }

    if (this.lineas.length === 0) {
      this.errorGeneral = 'El pedido debe tener al menos una línea de producto.';
      return;
    }

    // Validar líneas
    for (let i = 0; i < this.lineas.length; i++) {
      const l = this.lineas[i];
      if (!l.productoId) {
        this.errorGeneral = `La línea ${i + 1} no tiene un producto seleccionado.`;
        return;
      }
      if (!l.cantidad || l.cantidad <= 0) {
        this.errorGeneral = `La línea ${i + 1} tiene una cantidad inválida. La cantidad debe ser un número entero mayor que cero.`;
        return;
      }
    }

    const payload: CrearPedidoRequest = {
      clienteId: Number(this.clienteSeleccionadoId),
      confirmar,
      lineas: this.lineas.map(l => ({
        productoId: Number(l.productoId),
        cantidad: Math.floor(l.cantidad)
      }))
    };

    this.enviando = true;

    this.pedidosService.crearPedido(payload).subscribe({
      next: (pedidoCreado) => {
        this.enviando = false;
        // Navegar a la pantalla de detalle del pedido recién creado
        this.router.navigate(['/pedidos', pedidoCreado.id]);
      },
      error: (err) => {
        this.enviando = false;
        console.error('Error al crear pedido:', err);

        const errorResp: ApiErrorResponse = err?.error;
        if (err.status === 409 && errorResp?.detallesStock && errorResp.detallesStock.length > 0) {
          this.errorGeneral = errorResp.message || 'No hay existencias suficientes para confirmar el pedido.';
          this.detallesStock = errorResp.detallesStock;
        } else if (errorResp?.message) {
          this.errorGeneral = errorResp.message;
        } else {
          this.errorGeneral = 'Ocurrió un error inesperado al procesar el pedido. Intente nuevamente.';
        }
      }
    });
  }

  // Modal para crear nuevo cliente
  abrirModalCliente(): void {
    this.nuevoClienteNombre = '';
    this.nuevoClienteTelefono = '';
    this.errorClienteModal = null;
    this.mostrarModalCliente = true;
  }

  cerrarModalCliente(): void {
    this.mostrarModalCliente = false;
  }

  guardarNuevoCliente(): void {
    if (!this.nuevoClienteNombre.trim()) {
      this.errorClienteModal = 'El nombre del cliente es obligatorio.';
      return;
    }

    this.guardandoCliente = true;
    this.catalogoService.crearCliente({
      nombre: this.nuevoClienteNombre.trim(),
      telefono: this.nuevoClienteTelefono.trim()
    }).subscribe({
      next: (clienteCreado) => {
        this.guardandoCliente = false;
        this.clientes.push(clienteCreado);
        this.clienteSeleccionadoId = clienteCreado.id;
        this.cerrarModalCliente();
      },
      error: (err) => {
        this.guardandoCliente = false;
        this.errorClienteModal = err?.error?.message || 'Error al registrar el cliente.';
      }
    });
  }
}
