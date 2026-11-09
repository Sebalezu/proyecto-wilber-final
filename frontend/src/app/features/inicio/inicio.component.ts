import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { InventarioService } from '../../core/services/inventario.service';
import { PedidosService } from '../../core/services/pedidos.service';
import { Producto, PedidoResumen } from '../../shared/models';

@Component({
  selector: 'app-inicio',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './inicio.component.html',
  styleUrl: './inicio.component.scss'
})
export class InicioComponent implements OnInit {
  private readonly inventarioService = inject(InventarioService);
  private readonly pedidosService = inject(PedidosService);

  cargando = true;
  error: string | null = null;

  conteoPendientes = 0;
  conteoConfirmados = 0;
  conteoDespachados = 0;

  productosStockBajo: Producto[] = [];

  ngOnInit(): void {
    this.cargarDatosDashboard();
  }

  cargarDatosDashboard(): void {
    this.cargando = true;
    this.error = null;

    // Cargar pedidos para conteos
    this.pedidosService.getPedidos().subscribe({
      next: (pedidos: PedidoResumen[]) => {
        this.conteoPendientes = pedidos.filter(p => p.estado === 'PENDIENTE').length;
        this.conteoConfirmados = pedidos.filter(p => p.estado === 'CONFIRMADO').length;
        this.conteoDespachados = pedidos.filter(p => p.estado === 'DESPACHADO').length;
      },
      error: (err) => {
        console.error('Error al cargar pedidos:', err);
        this.error = 'No se pudo conectar con el servicio de pedidos.';
      }
    });

    // Cargar productos con stock bajo
    this.inventarioService.getStockBajo().subscribe({
      next: (productos: Producto[]) => {
        this.productosStockBajo = productos;
        this.cargando = false;
      },
      error: (err) => {
        console.error('Error al cargar stock bajo:', err);
        this.cargando = false;
      }
    });
  }
}
