import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { api } from '../services/api';
import type { InstrumentoResumen, Orden } from '../types/trading';

interface OrdenesViewProps {
  inversionistaId: string;
  instrumentoInicial?: string;
}

export function OrdenesView({ inversionistaId, instrumentoInicial }: OrdenesViewProps) {
  const [instrumentos, setInstrumentos] = useState<InstrumentoResumen[]>([]);
  const [misOrdenes, setMisOrdenes] = useState<Orden[]>([]);
  const [libroCompras, setLibroCompras] = useState<Orden[]>([]);
  const [libroVentas, setLibroVentas] = useState<Orden[]>([]);

  // Campos del formulario
  const [lado, setLado] = useState<'COMPRA' | 'VENTA'>('COMPRA');
  const [instrumentoId, setInstrumentoId] = useState<string>(instrumentoInicial || 'AAPL');
  const [tipo, setTipo] = useState<'LIMITE' | 'MERCADO'>('LIMITE');
  const [cantidad, setCantidad] = useState<string>('10');
  const [precioLimite, setPrecioLimite] = useState<string>('185.00');

  const [enviando, setEnviando] = useState(false);
  const [notificacion, setNotificacion] = useState<{ texto: string; esError: boolean } | null>(null);

  const avisar = (texto: string, esError = false) => {
    setNotificacion({ texto, esError });
    setTimeout(() => setNotificacion(null), 5000);
  };

  const cargarDatos = useCallback(async () => {
    try {
      const [listaInst, ordenesUsuario, compras, ventas] = await Promise.all([
        api.listarInstrumentos(),
        api.misOrdenes(inversionistaId),
        api.consultarLibro(instrumentoId, 'COMPRA'),
        api.consultarLibro(instrumentoId, 'VENTA'),
      ]);

      setInstrumentos(listaInst);
      setMisOrdenes(ordenesUsuario);
      setLibroCompras(compras);
      setLibroVentas(ventas);
    } catch (err) {
      console.error('Error cargando órdenes:', err);
    }
  }, [inversionistaId, instrumentoId]);

  useEffect(() => {
    cargarDatos();
    const timer = setInterval(cargarDatos, 4000);
    return () => clearInterval(timer);
  }, [cargarDatos]);

  // Sincroniza precio inicial cuando carga la lista de instrumentos por primera vez
  useEffect(() => {
    if (instrumentos.length > 0 && !precioLimite) {
      const actual = instrumentos.find((i) => i.id === instrumentoId) ?? instrumentos[0];
      setInstrumentoId(actual.id);
      setPrecioLimite(Number(actual.precioActual).toFixed(2));
    }
  }, [instrumentos, instrumentoId, precioLimite]);

  const handleCambiarInstrumento = (nuevoId: string) => {
    setInstrumentoId(nuevoId);
    const encontrado = instrumentos.find((i) => i.id === nuevoId);
    if (encontrado) {
      setPrecioLimite(Number(encontrado.precioActual).toFixed(2));
    }
  };

  const handleConfirmarOrden = async (e: FormEvent) => {
    e.preventDefault();
    setEnviando(true);
    setNotificacion(null);

    try {
      const res = await api.crearOrden({
        inversionistaId,
        instrumentoId,
        lado,
        tipo,
        cantidad: Number(cantidad),
        precioLimite: tipo === 'LIMITE' ? Number(precioLimite) : null,
      });

      if (res.transaccionEjecutada) {
        avisar(
          `Orden ejecutada: ${res.transaccionEjecutada.cantidad} ${res.transaccionEjecutada.instrumentoId} a $${Number(
            res.transaccionEjecutada.precioEjecucion
          ).toFixed(2)}`
        );
      } else {
        avisar(
          `Orden de ${res.orden.lado} (${res.orden.cantidad} ${res.orden.instrumentoId}) registrada como PENDIENTE.`
        );
      }
      await cargarDatos();
    } catch (err) {
      avisar(err instanceof Error ? err.message : 'Error al registrar la orden', true);
    } finally {
      setEnviando(false);
    }
  };

  const handleCancelarOrden = async (ordenId: string) => {
    try {
      await api.cancelarOrden(ordenId, inversionistaId);
      avisar('Orden cancelada correctamente.');
      await cargarDatos();
    } catch (err) {
      avisar(err instanceof Error ? err.message : 'No se pudo cancelar la orden', true);
    }
  };

  return (
    <main className="max-w-7xl mx-auto px-6 py-10 space-y-10">
      {/* Notificación */}
      {notificacion && (
        <div
          className={`px-4 py-3 rounded-lg border-2 border-black text-sm font-bold flex items-center justify-between ${
            notificacion.esError
              ? 'bg-black text-yellow-400 shadow-[3px_3px_0px_0px_#facc15]'
              : 'bg-yellow-400 text-black shadow-[3px_3px_0px_0px_#000000]'
          }`}
        >
          <span>{notificacion.texto}</span>
          <button
            type="button"
            onClick={() => setNotificacion(null)}
            className="text-xs underline ml-4 cursor-pointer"
          >
            Cerrar
          </button>
        </div>
      )}

      {/* Encabezado de Sección */}
      <div className="pb-4 border-b-2 border-black">
        <h1 className="text-3xl font-black tracking-tight text-black">Nueva Orden</h1>
        <p className="text-sm font-medium text-neutral-600 mt-1">
          Compra o venta sobre un instrumento
        </p>
      </div>

      {/* Formulario de Nueva Orden */}
      <section className="bg-white border-2 border-black rounded-lg p-6 shadow-[4px_4px_0px_0px_#000000]">
        <form onSubmit={handleConfirmarOrden} className="space-y-6">
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
            {/* Operación */}
            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-black mb-1.5">
                Operación
              </label>
              <select
                value={lado}
                onChange={(e) => setLado(e.target.value as 'COMPRA' | 'VENTA')}
                className="w-full bg-stone-50 border-2 border-black rounded-lg px-3 py-2.5 text-sm font-bold text-black focus:bg-yellow-50 focus:outline-none"
              >
                <option value="COMPRA">COMPRA</option>
                <option value="VENTA">VENTA</option>
              </select>
            </div>

            {/* Instrumento */}
            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-black mb-1.5">
                Instrumento
              </label>
              <select
                value={instrumentoId}
                onChange={(e) => handleCambiarInstrumento(e.target.value)}
                className="w-full bg-stone-50 border-2 border-black rounded-lg px-3 py-2.5 text-sm font-mono font-bold text-black focus:bg-yellow-50 focus:outline-none"
              >
                {instrumentos.map((inst) => (
                  <option key={inst.id} value={inst.id}>
                    {inst.id} - {inst.nombre} (${Number(inst.precioActual).toFixed(2)})
                  </option>
                ))}
              </select>
            </div>

            {/* Tipo de orden */}
            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-black mb-1.5">
                Tipo de orden
              </label>
              <select
                value={tipo}
                onChange={(e) => setTipo(e.target.value as 'LIMITE' | 'MERCADO')}
                className="w-full bg-stone-50 border-2 border-black rounded-lg px-3 py-2.5 text-sm font-bold text-black focus:bg-yellow-50 focus:outline-none"
              >
                <option value="LIMITE">LÍMITE</option>
                <option value="MERCADO">MERCADO</option>
              </select>
            </div>

            {/* Cantidad */}
            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-black mb-1.5">
                Cantidad
              </label>
              <input
                type="number"
                min="1"
                step="1"
                required
                value={cantidad}
                onChange={(e) => setCantidad(e.target.value)}
                className="w-full bg-stone-50 border-2 border-black rounded-lg px-3 py-2.5 text-sm font-mono font-bold text-black focus:bg-yellow-50 focus:outline-none"
              />
            </div>

            {/* Precio Límite */}
            <div className={tipo === 'MERCADO' ? 'opacity-45' : ''}>
              <label className="block text-xs font-bold uppercase tracking-wider text-black mb-1.5">
                Precio límite ($)
              </label>
              <input
                type="number"
                min="0.01"
                step="0.01"
                required={tipo === 'LIMITE'}
                disabled={tipo === 'MERCADO'}
                value={precioLimite}
                onChange={(e) => setPrecioLimite(e.target.value)}
                placeholder={tipo === 'MERCADO' ? 'Precio mercado' : '0.00'}
                className="w-full bg-stone-50 border-2 border-black rounded-lg px-3 py-2.5 text-sm font-mono font-bold text-black focus:bg-yellow-50 focus:outline-none disabled:cursor-not-allowed"
              />
            </div>
          </div>

          {/* Botón a la derecha */}
          <div className="flex justify-end pt-2 border-t border-neutral-200">
            <button
              type="submit"
              disabled={enviando}
              className="bg-yellow-400 hover:bg-yellow-300 disabled:opacity-50 text-black font-black text-sm px-6 py-2.5 rounded-lg border-2 border-black shadow-[3px_3px_0px_0px_#000000] active:translate-x-[1px] active:translate-y-[1px] transition cursor-pointer"
            >
              {enviando ? 'Procesando...' : 'Confirmar Orden'}
            </button>
          </div>
        </form>
      </section>

      {/* Sección inferior: Mis Órdenes y Libro del Instrumento */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        {/* Mis Órdenes (8 columnas) */}
        <section className="lg:col-span-8 space-y-3">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-black text-black">Mis Órdenes ({inversionistaId})</h2>
            <span className="text-xs font-medium text-neutral-500">
              {misOrdenes.length} registradas
            </span>
          </div>

          <div className="bg-white border-2 border-black rounded-lg overflow-hidden shadow-[4px_4px_0px_0px_#000000]">
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse text-sm">
                <thead>
                  <tr className="bg-black text-white text-xs uppercase tracking-wider">
                    <th className="py-3 px-4 font-bold">Instrumento</th>
                    <th className="py-3 px-4 font-bold">Operación</th>
                    <th className="py-3 px-4 font-bold">Tipo</th>
                    <th className="py-3 px-4 font-bold text-right">Cantidad</th>
                    <th className="py-3 px-4 font-bold text-right">Precio Límite</th>
                    <th className="py-3 px-4 font-bold">Estado</th>
                    <th className="py-3 px-4 font-bold text-right">Acción</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-neutral-200 font-mono">
                  {misOrdenes.length === 0 ? (
                    <tr>
                      <td colSpan={7} className="py-8 text-center font-sans text-neutral-500">
                        Aún no has creado ninguna orden.
                      </td>
                    </tr>
                  ) : (
                    misOrdenes.map((orden) => (
                      <tr key={orden.id} className="hover:bg-yellow-50/60">
                        <td className="py-3 px-4 font-black text-black">{orden.instrumentoId}</td>
                        <td className="py-3 px-4 font-sans font-bold">
                          <span
                            className={`px-2 py-0.5 rounded text-xs ${
                              orden.lado === 'COMPRA'
                                ? 'bg-yellow-400 text-black'
                                : 'bg-black text-yellow-400'
                            }`}
                          >
                            {orden.lado}
                          </span>
                        </td>
                        <td className="py-3 px-4 font-sans text-xs font-semibold text-neutral-700">
                          {orden.tipo}
                        </td>
                        <td className="py-3 px-4 text-right font-bold">{orden.cantidad} u.</td>
                        <td className="py-3 px-4 text-right">
                          {orden.precioLimite
                            ? `$${Number(orden.precioLimite).toFixed(2)}`
                            : 'MERCADO'}
                        </td>
                        <td className="py-3 px-4 font-sans">
                          <span
                            className={`text-[11px] font-bold px-2 py-0.5 rounded border ${
                              orden.estado === 'EJECUTADA'
                                ? 'bg-black text-white border-black'
                                : orden.estado === 'PENDIENTE'
                                ? 'bg-yellow-100 text-black border-black'
                                : 'bg-neutral-100 text-neutral-500 border-neutral-300'
                            }`}
                          >
                            {orden.estado}
                          </span>
                        </td>
                        <td className="py-3 px-4 text-right font-sans">
                          {orden.estado === 'PENDIENTE' ? (
                            <button
                              type="button"
                              onClick={() => handleCancelarOrden(orden.id)}
                              className="text-xs font-bold underline text-black hover:text-neutral-600 cursor-pointer"
                            >
                              Cancelar
                            </button>
                          ) : (
                            <span className="text-neutral-400 text-xs">—</span>
                          )}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </section>

        {/* Libro de Órdenes Pendientes del Mercado (4 columnas) */}
        <section className="lg:col-span-4 space-y-3">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-black text-black">Libro · {instrumentoId}</h2>
            <span className="text-xs font-bold bg-yellow-400 text-black px-2 py-0.5 rounded border border-black">
              Pendientes
            </span>
          </div>

          <div className="bg-white border-2 border-black rounded-lg p-4 shadow-[4px_4px_0px_0px_#000000] space-y-4">
            {/* Ofertas de Venta */}
            <div>
              <p className="text-xs font-black uppercase tracking-wider text-black mb-2">
                Ventas disponibles (Ask)
              </p>
              {libroVentas.length === 0 ? (
                <p className="text-xs text-neutral-500 italic">Sin ventas pendientes</p>
              ) : (
                <div className="space-y-1.5 font-mono text-xs">
                  {libroVentas.map((v) => (
                    <div
                      key={v.id}
                      className="flex justify-between items-center bg-stone-100 border border-neutral-300 px-3 py-1.5 rounded"
                    >
                      <span className="text-neutral-600">{v.inversionistaId}</span>
                      <span className="font-bold">{v.cantidad} u.</span>
                      <span className="font-black text-black">
                        {v.precioLimite ? `$${Number(v.precioLimite).toFixed(2)}` : 'MERCADO'}
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="border-t-2 border-black" />

            {/* Ofertas de Compra */}
            <div>
              <p className="text-xs font-black uppercase tracking-wider text-black mb-2">
                Compras disponibles (Bid)
              </p>
              {libroCompras.length === 0 ? (
                <p className="text-xs text-neutral-500 italic">Sin compras pendientes</p>
              ) : (
                <div className="space-y-1.5 font-mono text-xs">
                  {libroCompras.map((c) => (
                    <div
                      key={c.id}
                      className="flex justify-between items-center bg-yellow-50 border border-yellow-400 px-3 py-1.5 rounded"
                    >
                      <span className="text-neutral-700">{c.inversionistaId}</span>
                      <span className="font-bold">{c.cantidad} u.</span>
                      <span className="font-black text-black">
                        {c.precioLimite ? `$${Number(c.precioLimite).toFixed(2)}` : 'MERCADO'}
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </section>
      </div>
    </main>
  );
}