export type TipoMovimiento = 'DESCUENTO' | 'RESTITUCION' | 'AJUSTE';

export interface MovimientoInventario {
  id: number;
  productoId: number;
  pedidoId: number | null;
  tipo: TipoMovimiento;
  cantidad: number;
  fecha: string;
}

export interface AjusteInventarioRequest {
  productoId: number;
  cantidad: number;
  motivo?: string;
}

