import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, tap, catchError, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, UsuarioSesion, RolUsuario } from '../../shared/models';

const STORAGE_KEY = 'ladorada_usuario';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly baseUrl = environment.apiUrl;

  private readonly usuarioActualSubject = new BehaviorSubject<UsuarioSesion | null>(this.obtenerUsuarioAlmacenado());
  readonly usuarioActual$: Observable<UsuarioSesion | null> = this.usuarioActualSubject.asObservable();

  private obtenerUsuarioAlmacenado(): UsuarioSesion | null {
    try {
      const data = localStorage.getItem(STORAGE_KEY);
      return data ? (JSON.parse(data) as UsuarioSesion) : null;
    } catch (e) {
      console.error('Error al leer usuario de localStorage:', e);
      return null;
    }
  }

  login(email: string, password: string): Observable<UsuarioSesion> {
    const payload: LoginRequest = { email: email.trim(), password: password.trim() };
    return this.http.post<UsuarioSesion>(`${this.baseUrl}/auth/login`, payload).pipe(
      tap((usuario) => {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(usuario));
        this.usuarioActualSubject.next(usuario);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(STORAGE_KEY);
    this.usuarioActualSubject.next(null);
    this.router.navigate(['/login']);
  }

  getUsuarioActual(): UsuarioSesion | null {
    return this.usuarioActualSubject.value;
  }

  getRol(): RolUsuario | null {
    return this.usuarioActualSubject.value?.rol || null;
  }

  hasRole(rolesPermitidos: string[]): boolean {
    const rolActual = this.getRol();
    if (!rolActual) return false;
    return rolesPermitidos.includes(rolActual);
  }

  isAuthenticated(): boolean {
    return !!this.getUsuarioActual();
  }

  getUsuariosDisponibles(): Observable<UsuarioSesion[]> {
    return this.http.get<UsuarioSesion[]>(`${this.baseUrl}/auth/usuarios`).pipe(
      catchError(() => of([
        { id: 1, nombre: 'Gerencia / Admin', email: 'admin@ladorada.com', rol: 'ADMINISTRADOR' as RolUsuario },
        { id: 2, nombre: 'Carlos Ventas', email: 'ventas@ladorada.com', rol: 'VENDEDOR' as RolUsuario },
        { id: 3, nombre: 'Marta Bodega', email: 'bodega@ladorada.com', rol: 'BODEGA' as RolUsuario }
      ]))
    );
  }
}
