import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { api } from '../services/api';

interface FilaCotizacion {
  id: string;
  nombre: string;
  tipo: 'ACCION' | 'ETF';
  precio: number;
  variacionPorcentaje: number;
  volumen: number;
}

interface MercadoViewProps {
  inversionistaId: string;
}

export function MercadoView({ inversionistaId }: MercadoViewProps) {
  const [filas, setFilas] = useState<FilaCotizacion[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [notificacion, setNotificacion] = useState<string | null>(null);

  // Estado del modal de Nueva Orden
  const [modalAbierto, setModalAbierto] = useState(false);
  const [instrumentoOrden, setInstrumentoOrden] = useState('AAPL');
  const [lado, setLado] = useState<'COMPRA' | 'VENTA'>('COMPRA');
  const [tipo, setTipo] = useState<'LIMITE' | 'MERCADO'>('LIMITE');
  const [cantidad, setCantidad] = useState('10');
  const [precioLimite, setPrecioLimite] = useState('');
  const [enviandoOrden, setEnviandoOrden] = useState(false);
  const [errorOrden, setErrorOrden] = useState<string | null>(null);

  const cargarCotizaciones = useCallback(async () => {
    try {
      const instrumentos = await api.listarInstrumentos();

      const detalleFilas = await Promise.all(
        instrumentos.map(async (inst) => {
          const [historial, compras, ventas] = await Promise.all([
            api.historialInstrumento(inst.id),
            api.consultarLibro(inst.id, 'COMPRA'),
            api.consultarLibro(inst.id, 'VENTA'),
          ]);

          const precioActual = Number(inst.precioActual);
          let variacionPorcentaje = 0;

          if (historial.length >= 2) {
            const precioAnterior = Number(historial[historial.length - 2].precio);
            if (precioAnterior > 0) {
              variacionPorcentaje = ((precioActual - precioAnterior) / precioAnterior) * 100;
            }
          }

          const volumenLibro = [...compras, ...ventas].reduce(
            (acc, orden) => acc + Number(orden.cantidad),
            0
          );

          return {
            id: inst.id,
            nombre: inst.nombre,
            tipo: inst.tipo,
            precio: precioActual,
            variacionPorcentaje,
            volumen: volumenLibro,
          };
        })
      );

      setFilas(detalleFilas);
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al cargar el mercado');
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    cargarCotizaciones();
    const timer = setInterval(cargarCotizaciones, 4000);
    return () => clearInterval(timer);
  }, [cargarCotizaciones]);

  const abrirNuevaOrden = (simboloPreseleccionado?: string) => {
    const target = simboloPreseleccionado || filas[0]?.id || 'AAPL';
    const filaEncontrada = filas.find((f) => f.id === target);
    setInstrumentoOrden(target);
    setPrecioLimite(filaEncontrada ? filaEncontrada.precio.toFixed(2) : '');
    setLado('COMPRA');
    setTipo('LIMITE');
    setCantidad('10');
    setErrorOrden(null);
    setModalAbierto(true);
  };

  const handleCrearOrden = async (e: FormEvent) => {
    e.preventDefault();
    setErrorOrden(null);
    setEnviandoOrden(true);

    try {
      const res = await api.crearOrden({
        inversionistaId,
        instrumentoId: instrumentoOrden,
        lado,
        tipo,
        cantidad: Number(cantidad),
        precioLimite: tipo === 'LIMITE' ? Number(precioLimite) : null,
      });

      setModalAbierto(false);
      if (res.transaccionEjecutada) {
        setNotificacion(
          `Orden ejecutada: ${res.transaccionEjecutada.cantidad} ${res.transaccionEjecutada.instrumentoId} a $${Number(
            res.transaccionEjecutada.precioEjecucion
          ).toFixed(2)}`
        );
      } else {
        setNotificacion(
          `Nueva orden de ${res.orden.lado} (${res.orden.cantidad} ${res.orden.instrumentoId}) registrada como PENDIENTE.`
        );
      }
      setTimeout(() => setNotificacion(null), 5000);
      await cargarCotizaciones();
    } catch (err) {
      setErrorOrden(err instanceof Error ? err.message : 'No se pudo crear la orden');
    } finally {
      setEnviandoOrden(false);
    }
  };

  return (
    <main className="max-w-7xl mx-auto px-6 py-10">
      {/* Notificación superior */}
      {notificacion && (
        <div className="mb-6 bg-black text-yellow-400 border-2 border-black rounded-lg px-4 py-3 text-sm font-bold flex items-center justify-between shadow-[3px_3px_0px_0px_#facc15]">
          <span>{notificacion}</span>
          <button
            type="button"
            onClick={() => setNotificacion(null)}
            className="text-xs underline ml-4 cursor-pointer"
          >
            Cerrar
          </button>
        </div>
      )}

      {/* Encabezado de sección + Botón Nueva Orden */}
      <div className="flex flex-wrap items-end justify-between gap-4 mb-6 pb-4 border-b-2 border-black">
        <div>
          <h1 className="text-3xl font-black tracking-tight text-black">
            Panel de Cotizaciones
          </h1>
          <p className="text-sm font-medium text-neutral-600 mt-1">
            Instrumentos disponibles en tiempo simulado
          </p>
        </div>

        <button
          type="button"
          onClick={() => abrirNuevaOrden()}
          className="bg-yellow-400 hover:bg-yellow-300 text-black font-black text-sm px-5 py-2.5 rounded-lg border-2 border-black shadow-[3px_3px_0px_0px_#000000] active:translate-x-[1px] active:translate-y-[1px] transition cursor-pointer"
        >
          + Nueva Orden
        </button>
      </div>

      {error && (
        <div className="mb-6 bg-black text-yellow-400 border-l-4 border-yellow-400 px-4 py-3 text-sm font-medium">
          {error}
        </div>
      )}

      {/* Tabla de Cotizaciones */}
      <div className="bg-white border-2 border-black rounded-lg overflow-hidden shadow-[4px_4px_0px_0px_#000000]">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-black text-white text-xs uppercase tracking-wider">
                <th className="py-4 px-6 font-bold">Instrumento</th>
                <th className="py-4 px-6 font-bold text-right">Precio</th>
                <th className="py-4 px-6 font-bold text-right">Variación</th>
                <th className="py-4 px-6 font-bold text-right">Volumen</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-neutral-200 text-sm">
              {cargando ? (
                <tr>
                  <td colSpan={4} className="py-10 text-center text-neutral-500">
                    Cargando cotizaciones del mercado...
                  </td>
                </tr>
              ) : filas.length === 0 ? (
                <tr>
                  <td colSpan={4} className="py-10 text-center text-neutral-500">
                    No hay instrumentos disponibles en el mercado.
                  </td>
                </tr>
              ) : (
                filas.map((fila) => {
                  const esPositiva = fila.variacionPorcentaje > 0;
                  const esNegativa = fila.variacionPorcentaje < 0;

                  return (
                    <tr
                      key={fila.id}
                      onClick={() => abrirNuevaOrden(fila.id)}
                      title="Haz clic para crear una orden con este instrumento"
                      className="hover:bg-yellow-50/80 transition cursor-pointer"
                    >
                      {/* Instrumento */}
                      <td className="py-4 px-6">
                        <div className="flex items-center gap-3">
                          <span className="font-mono font-black text-base text-black">
                            {fila.id}
                          </span>
                          <span className="text-[10px] font-bold uppercase px-2 py-0.5 bg-stone-100 border border-neutral-300 text-neutral-700 rounded">
                            {fila.tipo}
                          </span>
                        </div>
                        <p className="text-xs text-neutral-500 mt-0.5">{fila.nombre}</p>
                      </td>

                      {/* Precio */}
                      <td className="py-4 px-6 text-right font-mono font-bold text-base text-black">
                        ${fila.precio.toFixed(2)}
                      </td>

                      {/* Variación */}
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
                          {esPositiva ? '+' : ''}
                          {fila.variacionPorcentaje.toFixed(2)}%
                        </span>
                      </td>

                      {/* Volumen */}
                      <td className="py-4 px-6 text-right font-mono font-semibold text-neutral-800">
                        {fila.volumen.toLocaleString('es-CO')} u.
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal para Crear Nueva Orden */}
      {modalAbierto && (
        <div className="fixed inset-0 z-50 bg-black/60 flex items-center justify-center p-4">
          <div className="bg-stone-50 border-2 border-black rounded-xl w-full max-w-md p-6 shadow-[6px_6px_0px_0px_#facc15] space-y-5">
            <div className="flex items-center justify-between border-b-2 border-black pb-3">
              <div>
                <h2 className="text-xl font-black text-black">Nueva Orden</h2>
                <p className="text-xs text-neutral-600">Cuenta: {inversionistaId}</p>
              </div>
              <button
                type="button"
                onClick={() => setModalAbierto(false)}
                className="w-8 h-8 rounded bg-black text-white font-bold text-sm hover:bg-neutral-800 cursor-pointer"
              >
                ✕
              </button>
            </div>

            {errorOrden && (
              <div className="bg-black text-yellow-400 border-l-4 border-yellow-400 px-3 py-2 text-xs font-medium">
                {errorOrden}
              </div>
            )}

            <form onSubmit={handleCrearOrden} className="space-y-4 text-sm">
              <div>
                <label className="block text-xs font-bold uppercase text-black mb-1">
                  Instrumento
                </label>
                <select
                  value={instrumentoOrden}
                  onChange={(e) => {
                    const id = e.target.value;
                    setInstrumentoOrden(id);
                    const f = filas.find((item) => item.id === id);
                    if (f) setPrecioLimite(f.precio.toFixed(2));
                  }}
                  className="w-full bg-white border-2 border-black rounded-lg px-3 py-2 font-mono font-bold text-black"
                >
                  {filas.map((f) => (
                    <option key={f.id} value={f.id}>
                      {f.id} - {f.nombre} (${f.precio.toFixed(2)})
                    </option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold uppercase text-black mb-1">
                    Operación
                  </label>
                  <div className="grid grid-cols-2 border-2 border-black rounded-lg overflow-hidden">
                    <button
                      type="button"
                      onClick={() => setLado('COMPRA')}
                      className={`py-2 text-xs font-black cursor-pointer ${
                        lado === 'COMPRA'
                          ? 'bg-yellow-400 text-black'
                          : 'bg-white text-neutral-600'
                      }`}
                    >
                      COMPRA
                    </button>
                    <button
                      type="button"
                      onClick={() => setLado('VENTA')}
                      className={`py-2 text-xs font-black cursor-pointer ${
                        lado === 'VENTA'
                          ? 'bg-black text-yellow-400'
                          : 'bg-white text-neutral-600'
                      }`}
                    >
                      VENTA
                    </button>
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-bold uppercase text-black mb-1">
                    Tipo
                  </label>
                  <select
                    value={tipo}
                    onChange={(e) => setTipo(e.target.value as 'LIMITE' | 'MERCADO')}
                    className="w-full bg-white border-2 border-black rounded-lg px-3 py-2 text-xs font-bold text-black"
                  >
                    <option value="LIMITE">LÍMITE</option>
                    <option value="MERCADO">MERCADO</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold uppercase text-black mb-1">
                    Cantidad
                  </label>
                  <input
                    type="number"
                    min="1"
                    step="1"
                    required
                    value={cantidad}
                    onChange={(e) => setCantidad(e.target.value)}
                    className="w-full bg-white border-2 border-black rounded-lg px-3 py-2 font-mono text-black"
                  />
                </div>

                <div className={tipo === 'MERCADO' ? 'opacity-40' : ''}>
                  <label className="block text-xs font-bold uppercase text-black mb-1">
                    Precio Límite ($)
                  </label>
                  <input
                    type="number"
                    min="0.01"
                    step="0.01"
                    required={tipo === 'LIMITE'}
                    disabled={tipo === 'MERCADO'}
                    value={precioLimite}
                    onChange={(e) => setPrecioLimite(e.target.value)}
                    className="w-full bg-white border-2 border-black rounded-lg px-3 py-2 font-mono text-black"
                  />
                </div>
              </div>

              <div className="pt-2 flex gap-3">
                <button
                  type="button"
                  onClick={() => setModalAbierto(false)}
                  className="w-1/3 bg-white hover:bg-neutral-100 border-2 border-black text-black font-bold py-2.5 rounded-lg text-xs cursor-pointer"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={enviandoOrden}
                  className="w-2/3 bg-yellow-400 hover:bg-yellow-300 border-2 border-black text-black font-black py-2.5 rounded-lg text-sm shadow-[2px_2px_0px_0px_#000000] cursor-pointer"
                >
                  {enviandoOrden ? 'Enviando...' : `Confirmar ${lado}`}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </main>
  );
}