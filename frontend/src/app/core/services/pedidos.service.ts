import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ActualizarPedidoRequest,
  CambiarEstadoPedidoRequest,
  CrearPedidoRequest,
  EstadoPedido,
  PedidoDetalle,
  PedidoResumen
} from '../../shared/models';

@Injectable({
  providedIn: 'root'
})
export class PedidosService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  getPedidos(estado?: EstadoPedido, clienteId?: number): Observable<PedidoResumen[]> {
    let params = new HttpParams();
    if (estado) {
      params = params.set('estado', estado);
    }
    if (clienteId) {
      params = params.set('clienteId', clienteId.toString());
    }
    return this.http.get<PedidoResumen[]>(`${this.baseUrl}/pedidos`, { params });
  }

  getPedidoById(id: number): Observable<PedidoDetalle> {
    return this.http.get<PedidoDetalle>(`${this.baseUrl}/pedidos/${id}`);
  }

  crearPedido(request: CrearPedidoRequest): Observable<PedidoDetalle> {
    return this.http.post<PedidoDetalle>(`${this.baseUrl}/pedidos`, request);
  }

  actualizarPedido(id: number, request: ActualizarPedidoRequest): Observable<PedidoDetalle> {
    return this.http.put<PedidoDetalle>(`${this.baseUrl}/pedidos/${id}`, request);
  }

  cambiarEstado(id: number, request: CambiarEstadoPedidoRequest): Observable<PedidoDetalle> {
    return this.http.patch<PedidoDetalle>(`${this.baseUrl}/pedidos/${id}/estado`, request);
  }
}

