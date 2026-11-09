import { Cliente } from './cliente.model';

export type EstadoPedido = 'PENDIENTE' | 'CONFIRMADO' | 'DESPACHADO' | 'RECHAZADO' | 'CANCELADO';

export type ClienteResumen = Cliente;

export interface PedidoLinea {
  id?: number;
  productoId: number;
  productoNombre?: string;
  productoCodigo?: string;
  cantidad: number;
  precioUnitario?: number;
  subtotal?: number;
}

export interface HistorialEstado {
  id: number;
  estadoAnterior: EstadoPedido | null;
  estadoNuevo: EstadoPedido;
  fecha: string;
}

export interface PedidoResumen {
  id: number;
  cliente: ClienteResumen;
  total: number;
  estado: EstadoPedido;
  stockDescontado: boolean;
  fecha: string;
  cantidadLineas?: number;
}

export interface PedidoDetalle {
  id: number;
  cliente: ClienteResumen;
  lineas: PedidoLinea[];
  total: number;
  estado: EstadoPedido;
  stockDescontado: boolean;
  fecha: string;
  historial: HistorialEstado[];
}

export interface CrearPedidoRequest {
  clienteId: number;
  confirmar?: boolean;
  lineas: { productoId: number; cantidad: number }[];
}

export interface ActualizarPedidoRequest {
  lineas: { productoId: number; cantidad: number }[];
}

export interface CambiarEstadoPedidoRequest {
  estado: EstadoPedido;
}

