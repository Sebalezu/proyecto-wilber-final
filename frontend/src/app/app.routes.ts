import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'inicio',
    pathMatch: 'full'
  },
  {
    path: 'inicio',
    loadComponent: () => import('./features/inicio/inicio.component').then(m => m.InicioComponent)
  },
  {
    path: 'catalogo',
    loadComponent: () => import('./features/catalogo/catalogo.component').then(m => m.CatalogoComponent)
  },
  {
    path: 'nuevo-pedido',
    loadComponent: () => import('./features/nuevo-pedido/nuevo-pedido.component').then(m => m.NuevoPedidoComponent)
  },
  {
    path: 'pedidos',
    loadComponent: () => import('./features/pedidos/pedidos.component').then(m => m.PedidosComponent)
  },
  {
    path: 'pedidos/:id',
    loadComponent: () => import('./features/pedido-detalle/pedido-detalle.component').then(m => m.PedidoDetalleComponent)
  },
  {
    path: '**',
    redirectTo: 'inicio'
  }
];
