import type {
  Cotizacion,
  CrearOrdenResponse,
  InstrumentoResumen,
  Orden,
  Portafolio,
  Transaccion,
  UsuarioSesion,
} from '../types/trading';

const API_URL = 'http://localhost:8082/api';

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_URL}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });

  if (res.status === 401) {
    throw new Error('Usuario o contraseña incorrectos');
  }

  const text = await res.text();
  const data = text ? JSON.parse(text) : null;

  if (!res.ok) {
    throw new Error(data?.mensaje || data?.message || `Error HTTP ${res.status}`);
  }

  return data as T;
}

export const api = {
  login: (username: string, password: string) =>
    request<UsuarioSesion>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    }),

  listarInstrumentos: () => request<InstrumentoResumen[]>('/mercado/instrumentos'),

  historialInstrumento: (id: string) =>
    request<Cotizacion[]>(`/mercado/instrumentos/${encodeURIComponent(id)}/historial`),

  registrarInstrumento: (payload: {
    id: string;
    nombre: string;
    tipo: 'ACCION' | 'ETF';
    precioInicial: number;
    adminId: string;
  }) =>
    request<InstrumentoResumen>('/mercado/instrumentos', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  crearInstrumento: (payload: {
    id: string;
    nombre: string;
    tipo: 'ACCION' | 'ETF';
    precioInicial: number;
    adminId?: string;
  }) =>
    request<InstrumentoResumen>('/mercado/instrumentos', {
      method: 'POST',
      body: JSON.stringify({
        adminId: payload.adminId ?? 'ADMIN-01',
        ...payload,
      }),
    }),

  actualizarPrecio: (
    instrumentoOrPayload:
      | string
      | { instrumentoId: string; nuevoPrecio: number; adminId: string },
    nuevoPrecio?: number,
    adminId = 'ADMIN-01'
  ) => {
    const body =
      typeof instrumentoOrPayload === 'string'
        ? {
            instrumentoId: instrumentoOrPayload,
            nuevoPrecio: nuevoPrecio ?? 0,
            adminId,
          }
        : instrumentoOrPayload;

    return request<Cotizacion>('/mercado/cotizaciones', {
      method: 'POST',
      body: JSON.stringify(body),
    });
  },

  consultarPortafolio: (inversionistaId: string) =>
    request<Portafolio>(`/portafolio/${encodeURIComponent(inversionistaId)}`),

  cargarPosicion: (payload: {
    inversionistaId: string;
    instrumentoId: string;
    cantidad: number;
    precio: number;
  }) =>
    request<Portafolio>('/portafolio/cargar', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  consultarLibro: (instrumentoId: string, lado: 'COMPRA' | 'VENTA') =>
    request<Orden[]>(
      `/ordenes/libro?instrumentoId=${encodeURIComponent(instrumentoId)}&lado=${lado}`
    ),

  misOrdenes: (inversionistaId: string) =>
    request<Orden[]>(`/ordenes?inversionistaId=${encodeURIComponent(inversionistaId)}`),

  misTransacciones: (inversionistaId: string) =>
    request<Transaccion[]>(
      `/ordenes/transacciones?inversionistaId=${encodeURIComponent(inversionistaId)}`
    ),

  crearOrden: (payload: {
    inversionistaId: string;
    instrumentoId: string;
    lado: 'COMPRA' | 'VENTA';
    tipo: 'LIMITE' | 'MERCADO';
    cantidad: number;
    precioLimite: number | null;
  }) =>
    request<CrearOrdenResponse>('/ordenes', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  cancelarOrden: (ordenId: string, inversionistaId: string) =>
    request<Orden>(
      `/ordenes/${encodeURIComponent(ordenId)}?inversionistaId=${encodeURIComponent(inversionistaId)}`,
      {
        method: 'DELETE',
      }
    ),
};