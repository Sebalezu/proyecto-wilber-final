import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: '',
    redirectTo: 'inicio',
    pathMatch: 'full'
  },
  {
    path: 'inicio',
    loadComponent: () => import('./features/inicio/inicio.component').then(m => m.InicioComponent),
    canActivate: [authGuard]
  },
  {
    path: 'catalogo',
    loadComponent: () => import('./features/catalogo/catalogo.component').then(m => m.CatalogoComponent),
    canActivate: [authGuard]
  },
  {
    path: 'nuevo-pedido',
    loadComponent: () => import('./features/nuevo-pedido/nuevo-pedido.component').then(m => m.NuevoPedidoComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMINISTRADOR', 'VENDEDOR'] }
  },
  {
    path: 'pedidos',
    loadComponent: () => import('./features/pedidos/pedidos.component').then(m => m.PedidosComponent),
    canActivate: [authGuard]
  },
  {
    path: 'pedidos/:id',
    loadComponent: () => import('./features/pedido-detalle/pedido-detalle.component').then(m => m.PedidoDetalleComponent),
    canActivate: [authGuard]
  },
  {
    path: '**',
    redirectTo: 'inicio'
  }
];
