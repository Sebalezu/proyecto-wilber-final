export interface ErrorCampo {
  campo: string;
  mensaje: string;
}

export interface DetalleStock {
  productoId: number;
  nombre: string;
  solicitado: number;
  disponible: number;
}

export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  errors?: ErrorCampo[];
  detallesStock?: DetalleStock[];
}
