import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { api } from '../services/api';
import type {
  Cotizacion,
  InstrumentoResumen,
  Orden,
  Portafolio,
  Transaccion,
  UsuarioSesion,
} from '../types/trading';

interface Props {
  sesion: UsuarioSesion;
}

export function InversionistaDashboard({ sesion }: Props) {
  const inversionistaId = sesion.inversionistaId ?? 'INV-01';

  const [instrumentos, setInstrumentos] = useState<InstrumentoResumen[]>([]);
  const [seleccionado, setSeleccionado] = useState<string>('AAPL');
  const [historial, setHistorial] = useState<Cotizacion[]>([]);
  const [libroCompras, setLibroCompras] = useState<Orden[]>([]);
  const [libroVentas, setLibroVentas] = useState<Orden[]>([]);
  const [portafolio, setPortafolio] = useState<Portafolio | null>(null);
  const [ordenes, setOrdenes] = useState<Orden[]>([]);
  const [transacciones, setTransacciones] = useState<Transaccion[]>([]);

  // Formulario de orden
  const [lado, setLado] = useState<'COMPRA' | 'VENTA'>('COMPRA');
  const [tipo, setTipo] = useState<'LIMITE' | 'MERCADO'>('LIMITE');
  const [cantidad, setCantidad] = useState<string>('10');
  const [precioLimite, setPrecioLimite] = useState<string>('185.50');
  const [enviando, setEnviando] = useState(false);

  // Notificación
  const [alerta, setAlerta] = useState<{ texto: string; tipo: 'ok' | 'error' } | null>(null);

  const notificar = (texto: string, tipoAlerta: 'ok' | 'error' = 'ok') => {
    setAlerta({ texto, tipo: tipoAlerta });
    setTimeout(() => setAlerta(null), 5000);
  };

  const refrescar = useCallback(async () => {
    try {
      const [listaInst, compras, ventas, port, misOrd, misTx, hist] = await Promise.all([
        api.listarInstrumentos(),
        api.consultarLibro(seleccionado, 'COMPRA'),
        api.consultarLibro(seleccionado, 'VENTA'),
        api.consultarPortafolio(inversionistaId),
        api.misOrdenes(inversionistaId),
        api.misTransacciones(inversionistaId),
        api.historialInstrumento(seleccionado),
      ]);
      setInstrumentos(listaInst);
      setLibroCompras(compras);
      setLibroVentas(ventas);
      setPortafolio(port);
      setOrdenes(misOrd);
      setTransacciones(misTx);
      setHistorial(hist);
    } catch (err) {
      console.error('Error refrescando datos:', err);
    }
  }, [inversionistaId, seleccionado]);

  useEffect(() => {
    refrescar();
    const id = setInterval(refrescar, 3000);
    return () => clearInterval(id);
  }, [refrescar]);

  const seleccionarActivo = (inst: InstrumentoResumen) => {
    setSeleccionado(inst.id);
    setPrecioLimite(Number(inst.precioActual).toFixed(2));
  };

  const handleCrearOrden = async (e: FormEvent) => {
    e.preventDefault();
    setEnviando(true);
    try {
      const res = await api.crearOrden({
        inversionistaId,
        instrumentoId: seleccionado,
        lado,
        tipo,
        cantidad: Number(cantidad),
        precioLimite: tipo === 'LIMITE' ? Number(precioLimite) : null,
      });

      if (res.transaccionEjecutada) {
        notificar(
          `¡Cruce ejecutado! ${res.transaccionEjecutada.cantidad} ${res.transaccionEjecutada.instrumentoId} a $${Number(
            res.transaccionEjecutada.precioEjecucion
          ).toFixed(2)}`
        );
      } else {
        notificar(`Orden de ${res.orden.lado} registrada como PENDIENTE en el libro.`);
      }
      await refrescar();
    } catch (err) {
      notificar(err instanceof Error ? err.message : 'Error al enviar la orden', 'error');
    } finally {
      setEnviando(false);
    }
  };

  const handleCancelar = async (ordenId: string) => {
    try {
      await api.cancelarOrden(ordenId, inversionistaId);
      notificar('Orden cancelada correctamente.');
      await refrescar();
    } catch (err) {
      notificar(err instanceof Error ? err.message : 'No se pudo cancelar la orden', 'error');
    }
  };

  const gpTotal = portafolio ? Number(portafolio.gananciaNoRealizada) : 0;

  return (
    <div className="max-w-7xl mx-auto p-6 space-y-6">
      {alerta && (
        <div
          className={`rounded-xl px-4 py-3 text-sm font-medium border ${
            alerta.tipo === 'error'
              ? 'bg-rose-950/80 border-rose-700 text-rose-200'
              : 'bg-emerald-950/80 border-emerald-700 text-emerald-200'
          }`}
        >
          {alerta.texto}
        </div>
      )}

      {/* Tarjetas de Mercado */}
      <section>
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-xs font-semibold uppercase tracking-wider text-slate-400">
            Mercado en Vivo · Selecciona un instrumento para operar
          </h2>
          <span className="text-xs text-slate-500">Sincronización cada 3s</span>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {instrumentos.map((inst) => {
            const activo = inst.id === seleccionado;
            return (
              <button
                key={inst.id}
                type="button"
                onClick={() => seleccionarActivo(inst)}
                className={`text-left rounded-xl p-4 border transition cursor-pointer ${
                  activo
                    ? 'bg-slate-900 border-emerald-500 ring-1 ring-emerald-500/40'
                    : 'bg-slate-900/80 border-slate-800 hover:border-slate-700'
                }`}
              >
                <div className="flex items-center justify-between">
                  <span className="font-mono font-bold text-base text-white">{inst.id}</span>
                  <span className="text-[10px] uppercase px-2 py-0.5 rounded bg-slate-800 text-slate-300">
                    {inst.tipo}
                  </span>
                </div>
                <p className="text-xs text-slate-400 truncate mt-0.5">{inst.nombre}</p>
                <p className="text-xl font-mono font-bold text-emerald-400 mt-2">
                  ${Number(inst.precioActual).toFixed(2)}
                </p>
              </button>
            );
          })}
        </div>
      </section>

      {/* Fila Principal: Boleta + Libro + Portafolio */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Boleta de Órdenes */}
        <section className="lg:col-span-4 bg-slate-900 border border-slate-800 rounded-xl p-5 space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-base font-bold text-white">Nueva Orden</h2>
            <span className="text-xs font-mono bg-slate-800 text-emerald-400 px-2.5 py-0.5 rounded">
              {seleccionado}
            </span>
          </div>

          <form onSubmit={handleCrearOrden} className="space-y-3 text-sm">
            <div>
              <label className="block text-slate-400 text-xs mb-1">Instrumento</label>
              <select
                value={seleccionado}
                onChange={(e) => {
                  const found = instrumentos.find((i) => i.id === e.target.value);
                  if (found) seleccionarActivo(found);
                }}
                className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white font-mono"
              >
                {instrumentos.map((i) => (
                  <option key={i.id} value={i.id}>
                    {i.id} - {i.nombre}
                  </option>
                ))}
              </select>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-slate-400 text-xs mb-1">Operación</label>
                <div className="grid grid-cols-2 bg-slate-800 p-1 rounded-lg">
                  <button
                    type="button"
                    onClick={() => setLado('COMPRA')}
                    className={`py-1.5 rounded text-xs font-semibold cursor-pointer ${
                      lado === 'COMPRA' ? 'bg-emerald-600 text-white' : 'text-slate-400'
                    }`}
                  >
                    COMPRA
                  </button>
                  <button
                    type="button"
                    onClick={() => setLado('VENTA')}
                    className={`py-1.5 rounded text-xs font-semibold cursor-pointer ${
                      lado === 'VENTA' ? 'bg-rose-600 text-white' : 'text-slate-400'
                    }`}
                  >
                    VENTA
                  </button>
                </div>
              </div>

              <div>
                <label className="block text-slate-400 text-xs mb-1">Tipo</label>
                <select
                  value={tipo}
                  onChange={(e) => setTipo(e.target.value as 'LIMITE' | 'MERCADO')}
                  className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white text-xs"
                >
                  <option value="LIMITE">LÍMITE</option>
                  <option value="MERCADO">MERCADO</option>
                </select>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-slate-400 text-xs mb-1">Cantidad</label>
                <input
                  type="number"
                  min="1"
                  step="1"
                  required
                  value={cantidad}
                  onChange={(e) => setCantidad(e.target.value)}
                  className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white font-mono"
                />
              </div>

              <div className={tipo === 'MERCADO' ? 'opacity-40' : ''}>
                <label className="block text-slate-400 text-xs mb-1">Precio Límite ($)</label>
                <input
                  type="number"
                  min="0.01"
                  step="0.01"
                  required={tipo === 'LIMITE'}
                  disabled={tipo === 'MERCADO'}
                  value={precioLimite}
                  onChange={(e) => setPrecioLimite(e.target.value)}
                  className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white font-mono"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={enviando}
              className={`w-full font-semibold py-2.5 rounded-lg transition cursor-pointer ${
                lado === 'COMPRA'
                  ? 'bg-emerald-600 hover:bg-emerald-500 text-white'
                  : 'bg-rose-600 hover:bg-rose-500 text-white'
              }`}
            >
              {enviando ? 'Enviando...' : `Enviar Orden de ${lado}`}
            </button>
          </form>
        </section>

        {/* Libro de Órdenes + Historial Reciente */}
        <section className="lg:col-span-4 bg-slate-900 border border-slate-800 rounded-xl p-5 flex flex-col justify-between space-y-4">
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h2 className="text-base font-bold text-white">Libro Pendiente ({seleccionado})</h2>
              <span className="text-[11px] text-slate-400">FIFO</span>
            </div>

            <div className="space-y-3 text-xs">
              <div>
                <h3 className="text-rose-400 font-semibold uppercase mb-1">Ventas (Ask)</h3>
                <div className="space-y-1 font-mono">
                  {libroVentas.length > 0 ? (
                    libroVentas.map((o) => (
                      <div
                        key={o.id}
                        className="flex justify-between bg-slate-950/70 px-2.5 py-1.5 rounded"
                      >
                        <span className="text-slate-400">{o.inversionistaId}</span>
                        <span>{o.cantidad} u.</span>
                        <span className="text-rose-400">
                          {o.precioLimite ? `$${Number(o.precioLimite).toFixed(2)}` : 'MERCADO'}
                        </span>
                      </div>
                    ))
                  ) : (
                    <p className="text-slate-600 italic">Sin ventas pendientes</p>
                  )}
                </div>
              </div>

              <div className="border-t border-slate-800 pt-3">
                <h3 className="text-emerald-400 font-semibold uppercase mb-1">Compras (Bid)</h3>
                <div className="space-y-1 font-mono">
                  {libroCompras.length > 0 ? (
                    libroCompras.map((o) => (
                      <div
                        key={o.id}
                        className="flex justify-between bg-slate-950/70 px-2.5 py-1.5 rounded"
                      >
                        <span className="text-slate-400">{o.inversionistaId}</span>
                        <span>{o.cantidad} u.</span>
                        <span className="text-emerald-400">
                          {o.precioLimite ? `$${Number(o.precioLimite).toFixed(2)}` : 'MERCADO'}
                        </span>
                      </div>
                    ))
                  ) : (
                    <p className="text-slate-600 italic">Sin compras pendientes</p>
                  )}
                </div>
              </div>
            </div>
          </div>

          <div className="border-t border-slate-800 pt-3">
            <p className="text-[11px] font-semibold uppercase text-slate-400 mb-1.5">
              Últimas Cotizaciones ({seleccionado})
            </p>
            <div className="flex flex-wrap gap-1.5 font-mono text-[11px]">
              {historial.slice(-5).map((c) => (
                <span
                  key={c.id}
                  className="bg-slate-950 border border-slate-800 px-2 py-0.5 rounded text-slate-300"
                >
                  ${Number(c.precio).toFixed(2)}
                </span>
              ))}
            </div>
          </div>
        </section>

        {/* Portafolio */}
        <section className="lg:col-span-4 bg-slate-900 border border-slate-800 rounded-xl p-5 space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-base font-bold text-white">Mi Portafolio</h2>
            <span className="text-xs font-mono text-slate-400">{inversionistaId}</span>
          </div>

          <div className="grid grid-cols-2 gap-3 bg-slate-950/70 border border-slate-800 rounded-lg p-3">
            <div>
              <p className="text-xs text-slate-400">Valor Total</p>
              <p className="text-lg font-bold font-mono text-white">
                ${portafolio ? Number(portafolio.valorTotal).toFixed(2) : '0.00'}
              </p>
            </div>
            <div>
              <p className="text-xs text-slate-400">Ganancia / Pérdida</p>
              <p
                className={`text-lg font-bold font-mono ${
                  gpTotal >= 0 ? 'text-emerald-400' : 'text-rose-400'
                }`}
              >
                {gpTotal >= 0 ? '+' : ''}${gpTotal.toFixed(2)}
              </p>
            </div>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-xs text-left">
              <thead className="text-slate-400 border-b border-slate-800">
                <tr>
                  <th className="py-1.5">Activo</th>
                  <th className="py-1.5 text-right">Cant.</th>
                  <th className="py-1.5 text-right">Costo</th>
                  <th className="py-1.5 text-right">Actual</th>
                  <th className="py-1.5 text-right">G/P</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 font-mono">
                {portafolio && portafolio.posiciones.length > 0 ? (
                  portafolio.posiciones.map((p) => {
                    const gp = Number(p.gananciaNoRealizada);
                    return (
                      <tr key={p.instrumentoId}>
                        <td className="py-2 font-bold text-white">{p.instrumentoId}</td>
                        <td className="py-2 text-right">{Number(p.cantidad)}</td>
                        <td className="py-2 text-right text-slate-400">
                          ${Number(p.costoPromedio).toFixed(2)}
                        </td>
                        <td className="py-2 text-right">${Number(p.precioActual).toFixed(2)}</td>
                        <td
                          className={`py-2 text-right ${
                            gp >= 0 ? 'text-emerald-400' : 'text-rose-400'
                          }`}
                        >
                          {gp >= 0 ? '+' : ''}
                          {gp.toFixed(2)}
                        </td>
                      </tr>
                    );
                  })
                ) : (
                  <tr>
                    <td colSpan={5} className="py-4 text-center text-slate-500">
                      Sin posiciones registradas
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </section>
      </div>

      {/* Fila Inferior: Mis Órdenes + Transacciones Ejecutadas */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <section className="bg-slate-900 border border-slate-800 rounded-xl p-5">
          <h2 className="text-base font-bold text-white mb-3">Mis Órdenes</h2>
          <div className="overflow-x-auto">
            <table className="w-full text-xs text-left">
              <thead className="text-slate-400 border-b border-slate-800">
                <tr>
                  <th className="py-2">Activo</th>
                  <th className="py-2">Operación</th>
                  <th className="py-2 text-right">Cant.</th>
                  <th className="py-2 text-right">Precio</th>
                  <th className="py-2">Estado</th>
                  <th className="py-2 text-right">Acción</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 font-mono">
                {ordenes.length > 0 ? (
                  ordenes.map((o) => {
                    const badge =
                      o.estado === 'EJECUTADA'
                        ? 'bg-emerald-950 text-emerald-400 border-emerald-800'
                        : o.estado === 'CANCELADA'
                        ? 'bg-slate-800 text-slate-400 border-slate-700'
                        : 'bg-amber-950 text-amber-300 border-amber-800';
                    return (
                      <tr key={o.id}>
                        <td className="py-2 font-bold text-white">{o.instrumentoId}</td>
                        <td className="py-2">
                          <span
                            className={
                              o.lado === 'COMPRA' ? 'text-emerald-400' : 'text-rose-400'
                            }
                          >
                            {o.lado}
                          </span>{' '}
                          · {o.tipo}
                        </td>
                        <td className="py-2 text-right">{o.cantidad}</td>
                        <td className="py-2 text-right">
                          {o.precioLimite ? `$${Number(o.precioLimite).toFixed(2)}` : 'MERCADO'}
                        </td>
                        <td className="py-2">
                          <span className={`px-2 py-0.5 rounded border text-[10px] ${badge}`}>
                            {o.estado}
                          </span>
                        </td>
                        <td className="py-2 text-right">
                          {o.estado === 'PENDIENTE' ? (
                            <button
                              type="button"
                              onClick={() => handleCancelar(o.id)}
                              className="text-rose-400 hover:underline cursor-pointer"
                            >
                              Cancelar
                            </button>
                          ) : (
                            '-'
                          )}
                        </td>
                      </tr>
                    );
                  })
                ) : (
                  <tr>
                    <td colSpan={6} className="py-4 text-center text-slate-500">
                      Sin órdenes registradas
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </section>

        <section className="bg-slate-900 border border-slate-800 rounded-xl p-5">
          <h2 className="text-base font-bold text-white mb-3">Transacciones Ejecutadas</h2>
          <div className="overflow-x-auto">
            <table className="w-full text-xs text-left">
              <thead className="text-slate-400 border-b border-slate-800">
                <tr>
                  <th className="py-2">Activo</th>
                  <th className="py-2">Comprador</th>
                  <th className="py-2">Vendedor</th>
                  <th className="py-2 text-right">Cant.</th>
                  <th className="py-2 text-right">Precio Ejec.</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 font-mono">
                {transacciones.length > 0 ? (
                  transacciones.map((t) => (
                    <tr key={t.id}>
                      <td className="py-2 font-bold text-white">{t.instrumentoId}</td>
                      <td className="py-2 text-emerald-400">{t.compradorId}</td>
                      <td className="py-2 text-rose-400">{t.vendedorId}</td>
                      <td className="py-2 text-right">{t.cantidad}</td>
                      <td className="py-2 text-right text-white font-semibold">
                        ${Number(t.precioEjecucion).toFixed(2)}
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={5} className="py-4 text-center text-slate-500">
                      Sin transacciones ejecutadas
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </section>
      </div>
    </div>
  );
}  