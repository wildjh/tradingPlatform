export type RolUsuario = 'INVERSIONISTA' | 'ADMIN';

export interface UsuarioSesion {
  token: string;
  username: string;
  nombreCompleto: string;
  rol: RolUsuario;
  inversionistaId: string | null;
}

export interface InstrumentoResumen {
  id: string;
  nombre: string;
  tipo: 'ACCION' | 'ETF';
  precioActual: number;
  ultimaActualizacion: string | null;
}

export interface Cotizacion {
  id: number;
  instrumentoId: string;
  precio: number;
  momento: string;
  origen: 'CARGA_INICIAL' | 'SIMULACION';
  generadoPor: string;
}

export interface PosicionPortafolio {
  instrumentoId: string;
  cantidad: number;
  costoPromedio: number;
  precioActual: number;
  valorActual: number;
  gananciaNoRealizada: number;
}

export interface Portafolio {
  inversionistaId: string;
  posiciones: PosicionPortafolio[];
  valorTotal: number;
  gananciaNoRealizada: number;
}

export interface Orden {
  id: string;
  inversionistaId: string;
  instrumentoId: string;
  lado: 'COMPRA' | 'VENTA';
  tipo: 'LIMITE' | 'MERCADO';
  cantidad: number;
  precioLimite: number | null;
  estado: 'PENDIENTE' | 'EJECUTADA' | 'CANCELADA';
  creadaEn: string;
}

export interface Transaccion {
  id: string;
  instrumentoId: string;
  ordenCompraId: string;
  ordenVentaId: string;
  compradorId: string;
  vendedorId: string;
  cantidad: number;
  precioEjecucion: number;
  ejecutadaEn: string;
}

export interface CrearOrdenResponse {
  orden: Orden;
  transaccionEjecutada: Transaccion | null;
}