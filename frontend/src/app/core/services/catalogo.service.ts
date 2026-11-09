import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Cliente,
  ClienteRequest,
  Producto,
  ProductoActivoRequest,
  ProductoRequest,
  ProductoUpdateRequest
} from '../../shared/models';

@Injectable({
  providedIn: 'root'
})
export class CatalogoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  // PRODUCTOS
  getProductos(q?: string, activo?: boolean): Observable<Producto[]> {
    let params = new HttpParams();
    if (q && q.trim()) {
      params = params.set('q', q.trim());
    }
    if (activo !== undefined && activo !== null) {
      params = params.set('activo', activo.toString());
    }
    return this.http.get<Producto[]>(`${this.baseUrl}/productos`, { params });
  }

  getProductoById(id: number): Observable<Producto> {
    return this.http.get<Producto>(`${this.baseUrl}/productos/${id}`);
  }

  crearProducto(request: ProductoRequest): Observable<Producto> {
    return this.http.post<Producto>(`${this.baseUrl}/productos`, request);
  }

  actualizarProducto(id: number, request: ProductoUpdateRequest): Observable<Producto> {
    return this.http.put<Producto>(`${this.baseUrl}/productos/${id}`, request);
  }

  cambiarEstadoActivo(id: number, activo: boolean): Observable<Producto> {
    const body: ProductoActivoRequest = { activo };
    return this.http.patch<Producto>(`${this.baseUrl}/productos/${id}/activo`, body);
  }

  // CLIENTES
  getClientes(q?: string): Observable<Cliente[]> {
    let params = new HttpParams();
    if (q && q.trim()) {
      params = params.set('q', q.trim());
    }
    return this.http.get<Cliente[]>(`${this.baseUrl}/clientes`, { params });
  }

  getClienteById(id: number): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.baseUrl}/clientes/${id}`);
  }

  crearCliente(request: ClienteRequest): Observable<Cliente> {
    return this.http.post<Cliente>(`${this.baseUrl}/clientes`, request);
  }
}
