import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { MovimientoInventario, Producto, TipoMovimiento } from '../../shared/models';

@Injectable({
  providedIn: 'root'
})
export class InventarioService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  getStockBajo(): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.baseUrl}/inventario/stock-bajo`);
  }

  getMovimientos(productoId?: number, pedidoId?: number, tipo?: TipoMovimiento): Observable<MovimientoInventario[]> {
    let params = new HttpParams();
    if (productoId) {
      params = params.set('productoId', productoId.toString());
    }
    if (pedidoId) {
      params = params.set('pedidoId', pedidoId.toString());
    }
    if (tipo) {
      params = params.set('tipo', tipo);
    }
    return this.http.get<MovimientoInventario[]>(`${this.baseUrl}/inventario/movimientos`, { params });
  }
}

