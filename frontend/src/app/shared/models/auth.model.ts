export type RolUsuario = 'ADMINISTRADOR' | 'VENDEDOR' | 'BODEGA';

export interface UsuarioSesion {
  id: number;
  nombre: string;
  email: string;
  rol: RolUsuario;
}

export interface LoginRequest {
  email: string;
  password: string;
}

