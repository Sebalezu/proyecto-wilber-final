export interface Producto {
  id: number;
  codigo: string;
  nombre: string;
  precio: number;
  existencias: number;
  activo: boolean;
  stockBajo: boolean;
  createdAt: string;
}

export interface ProductoRequest {
  codigo?: string;
  nombre: string;
  precio: number;
  existencias: number;
}

export interface ProductoUpdateRequest {
  nombre: string;
  precio: number;
  existencias: number;
}

export interface ProductoActivoRequest {
  activo: boolean;
}
