import { useCallback, useEffect, useState } from 'react';
import { api } from '../services/api';
import type { InstrumentoResumen, Portafolio, Transaccion } from '../types/trading';

interface PortafolioViewProps {
  inversionistaId: string;
  onIrAOrdenes?: () => void;
}

export function PortafolioView({ inversionistaId, onIrAOrdenes }: PortafolioViewProps) {
  const [portafolio, setPortafolio] = useState<Portafolio | null>(null);
  const [instrumentosMap, setInstrumentosMap] = useState<Record<string, InstrumentoResumen>>({});
  const [transacciones, setTransacciones] = useState<Transaccion[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const cargarPortafolio = useCallback(async () => {
    try {
      const [dataPortafolio, listaInstrumentos, historialTx] = await Promise.all([
        api.consultarPortafolio(inversionistaId),
        api.listarInstrumentos(),
        api.misTransacciones(inversionistaId).catch(() => []),
      ]);

      const mapa: Record<string, InstrumentoResumen> = {};
      for (const inst of listaInstrumentos) {
        mapa[inst.id] = inst;
      }

      setPortafolio(dataPortafolio);
      setInstrumentosMap(mapa);
      setTransacciones(historialTx);
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo cargar el portafolio');
    } finally {
      setCargando(false);
    }
  }, [inversionistaId]);

  useEffect(() => {
    cargarPortafolio();
    const timer = setInterval(cargarPortafolio, 4000);
    return () => clearInterval(timer);
  }, [cargarPortafolio]);

  const valorTotal = portafolio ? Number(portafolio.valorTotal) : 0;
  const gpTotal = portafolio ? Number(portafolio.gananciaNoRealizada) : 0;
  const costoInvertidoTotal = valorTotal - gpTotal;
  const rendimientoPorcentaje =
    costoInvertidoTotal > 0 ? (gpTotal / costoInvertidoTotal) * 100 : 0;
  const posiciones = portafolio?.posiciones ?? [];

  return (
    <main className="max-w-7xl mx-auto px-6 py-10 space-y-10">
      {/* Encabezado de Sección */}
      <div className="flex flex-wrap items-end justify-between gap-4 pb-4 border-b-2 border-black">
        <div>
          <h1 className="text-3xl font-black tracking-tight text-black">Mi Portafolio</h1>
          <p className="text-sm font-medium text-neutral-600 mt-1">
            Resumen de tenencias, valoración actual y movimientos ejecutados ·{' '}
            <span className="font-mono font-bold text-black">{inversionistaId}</span>
          </p>
        </div>

        {onIrAOrdenes && (
          <button
            type="button"
            onClick={onIrAOrdenes}
            className="bg-yellow-400 hover:bg-yellow-300 text-black font-black text-sm px-5 py-2.5 rounded-lg border-2 border-black shadow-[3px_3px_0px_0px_#000000] active:translate-x-[1px] active:translate-y-[1px] transition cursor-pointer"
          >
            + Operar en Mercado
          </button>
        )}
      </div>

      {error && (
        <div className="bg-black text-yellow-400 border-l-4 border-yellow-400 px-4 py-3 text-sm font-medium">
          {error}
        </div>
      )}

      {/* 1. Tarjetas de Resumen Financiero */}
      <section className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white border-2 border-black rounded-lg p-6 shadow-[4px_4px_0px_0px_#000000]">
          <p className="text-xs font-bold uppercase tracking-wider text-neutral-500">
            Valor Total del Portafolio
          </p>
          <p className="text-3xl font-mono font-black text-black mt-2">
            ${valorTotal.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </p>
          <p className="text-xs text-neutral-500 mt-2">
            Costo base invertido: ${costoInvertidoTotal.toFixed(2)}
          </p>
        </div>

        <div className="bg-white border-2 border-black rounded-lg p-6 shadow-[4px_4px_0px_0px_#000000]">
          <p className="text-xs font-bold uppercase tracking-wider text-neutral-500">
            Ganancia / Pérdida No Realizada
          </p>
          <div className="flex items-baseline gap-3 mt-2">
            <p className="text-3xl font-mono font-black text-black">
              {gpTotal >= 0 ? '+' : '-'}${Math.abs(gpTotal).toFixed(2)}
            </p>
            <span
              className={`font-mono text-xs font-black px-2 py-0.5 rounded ${
                gpTotal > 0
                  ? 'bg-yellow-400 text-black'
                  : gpTotal < 0
                  ? 'bg-black text-white'
                  : 'bg-stone-100 text-neutral-600'
              }`}
            >
              {gpTotal >= 0 ? '+' : ''}
              {rendimientoPorcentaje.toFixed(2)}%
            </span>
          </div>
          <p className="text-xs text-neutral-500 mt-2">
            Variación respecto a tu precio promedio de compra
          </p>
        </div>

        <div className="bg-white border-2 border-black rounded-lg p-6 shadow-[4px_4px_0px_0px_#000000]">
          <p className="text-xs font-bold uppercase tracking-wider text-neutral-500">
            Activos en Cartera
          </p>
          <p className="text-3xl font-mono font-black text-black mt-2">
            {posiciones.length}
          </p>
          <p className="text-xs text-neutral-500 mt-2">
            {transacciones.length} transacciones cerradas en total
          </p>
        </div>
      </section>

      {/* 2. Tabla de Posiciones Actuales */}
      <section className="space-y-3">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-black text-black">Posiciones Actuales</h2>
          <span className="inline-flex items-center gap-1.5 text-xs font-bold text-neutral-600">
            <span className="w-2 h-2 rounded-full bg-yellow-400 border border-black" />
            En vivo
          </span>
        </div>

        <div className="bg-white border-2 border-black rounded-lg overflow-hidden shadow-[4px_4px_0px_0px_#000000]">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-sm">
              <thead>
                <tr className="bg-black text-white text-xs uppercase tracking-wider">
                  <th className="py-4 px-6 font-bold">Instrumento</th>
                  <th className="py-4 px-6 font-bold text-right">Cantidad</th>
                  <th className="py-4 px-6 font-bold text-right">Costo Promedio</th>
                  <th className="py-4 px-6 font-bold text-right">Precio Actual</th>
                  <th className="py-4 px-6 font-bold text-right">Valor Total</th>
                  <th className="py-4 px-6 font-bold text-right">Ganancia / Pérdida</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-neutral-200">
                {cargando ? (
                  <tr>
                    <td colSpan={6} className="py-10 text-center text-neutral-500">
                      Obteniendo datos...
                    </td>
                  </tr>
                ) : posiciones.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="py-10 text-center text-neutral-500">
                      Aún no tienes activos en tu portafolio. Envía una orden de compra para empezar.
                    </td>
                  </tr>
                ) : (
                  posiciones.map((pos) => {
                    const info = instrumentosMap[pos.instrumentoId];
                    const gp = Number(pos.gananciaNoRealizada);
                    const esPositiva = gp > 0;
                    const esNegativa = gp < 0;

                    return (
                      <tr key={pos.instrumentoId} className="hover:bg-yellow-50/70 transition">
                        <td className="py-4 px-6">
                          <div className="flex items-center gap-2.5">
                            <span className="font-mono font-black text-base text-black">
                              {pos.instrumentoId}
                            </span>
                            {info && (
                              <span className="text-[10px] font-bold uppercase px-2 py-0.5 bg-stone-100 border border-neutral-300 text-neutral-700 rounded">
                                {info.tipo}
                              </span>
                            )}
                          </div>
                          {info && (
                            <p className="text-xs text-neutral-500 mt-0.5">{info.nombre}</p>
                          )}
                        </td>

                        <td className="py-4 px-6 text-right font-mono font-bold text-black">
                          {Number(pos.cantidad)} u.
                        </td>

                        <td className="py-4 px-6 text-right font-mono text-neutral-600">
                          ${Number(pos.costoPromedio).toFixed(2)}
                        </td>

                        <td className="py-4 px-6 text-right font-mono font-bold text-black">
                          ${Number(pos.precioActual).toFixed(2)}
                        </td>

                        <td className="py-4 px-6 text-right font-mono font-black text-black">
                          ${Number(pos.valorActual).toFixed(2)}
                        </td>

                        <td className="py-4 px-6 text-right font-mono font-bold">
                          <span
                            className={`inline-block px-2.5 py-1 rounded text-xs ${
                              esPositiva
                                ? 'bg-yellow-400 text-black'
                                : esNegativa
                                ? 'bg-black text-white'
                                : 'bg-stone-100 text-neutral-600'
                            }`}
                          >
                            {gp >= 0 ? '+' : '-'}${Math.abs(gp).toFixed(2)}
                          </span>
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>
        </div>
      </section>

      {/* 3. Historial de Transacciones Ejecutadas */}
      <section className="space-y-3">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-black text-black">Historial de Transacciones Ejecutadas</h2>
          <span className="text-xs font-medium text-neutral-500">
            Órdenes cerradas en el mercado
          </span>
        </div>

        <div className="bg-white border-2 border-black rounded-lg overflow-hidden shadow-[4px_4px_0px_0px_#000000]">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-sm">
              <thead>
                <tr className="bg-black text-white text-xs uppercase tracking-wider">
                  <th className="py-3.5 px-6 font-bold">Operación</th>
                  <th className="py-3.5 px-6 font-bold">Instrumento</th>
                  <th className="py-3.5 px-6 font-bold text-right">Cantidad</th>
                  <th className="py-3.5 px-6 font-bold text-right">Precio Ejecución</th>
                  <th className="py-3.5 px-6 font-bold text-right">Total Operado</th>
                  <th className="py-3.5 px-6 font-bold text-right">Fecha</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-neutral-200 font-mono">
                {transacciones.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="py-8 text-center font-sans text-neutral-500">
                      Sin transacciones ejecutadas todavía.
                    </td>
                  </tr>
                ) : (
                  transacciones.map((tx) => {
                    const esCompra = tx.compradorId === inversionistaId;
                    const cant = Number(tx.cantidad);
                    const precio = Number(tx.precioEjecucion);
                    const total = cant * precio;
                    const fecha = tx.ejecutadaEn
                      ? new Date(tx.ejecutadaEn).toLocaleString('es-CO', {
                          dateStyle: 'short',
                          timeStyle: 'short',
                        })
                      : '—';

                    return (
                      <tr key={tx.id} className="hover:bg-yellow-50/60">
                        <td className="py-3.5 px-6 font-sans">
                          <span
                            className={`text-xs font-black px-2.5 py-0.5 rounded ${
                              esCompra
                                ? 'bg-yellow-400 text-black'
                                : 'bg-black text-yellow-400'
                            }`}
                          >
                            {esCompra ? 'COMPRA' : 'VENTA'}
                          </span>
                        </td>
                        <td className="py-3.5 px-6 font-black text-black">{tx.instrumentoId}</td>
                        <td className="py-3.5 px-6 text-right">{cant} u.</td>
                        <td className="py-3.5 px-6 text-right">${precio.toFixed(2)}</td>
                        <td className="py-3.5 px-6 text-right font-bold text-black">
                          ${total.toFixed(2)}
                        </td>
                        <td className="py-3.5 px-6 text-right font-sans text-xs text-neutral-500">
                          {fecha}
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>
        </div>
      </section>
    </main>
  );
}