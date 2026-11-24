import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { api } from '../services/api';
import type { InstrumentoResumen, UsuarioSesion } from '../types/trading';

interface AdminDashboardProps {
  sesion: UsuarioSesion;
}

type TabAdmin = 'mercado' | 'nuevo';

export function AdminDashboard({ sesion }: AdminDashboardProps) {
  const [instrumentos, setInstrumentos] = useState<InstrumentoResumen[]>([]);
  const [cargando, setCargando] = useState(true);
  const [tabActiva, setTabActiva] = useState<TabAdmin>('mercado');
  const [mensaje, setMensaje] = useState<{ texto: string; esError: boolean } | null>(null);

  // Formulario de Nuevo Instrumento
  const [nuevoId, setNuevoId] = useState('');
  const [nuevoNombre, setNuevoNombre] = useState('');
  const [nuevoTipo, setNuevoTipo] = useState<'ACCION' | 'ETF'>('ACCION');
  const [nuevoPrecio, setNuevoPrecio] = useState('');

  // Formulario de Simulación de Precio
  const [instSeleccionado, setInstSeleccionado] = useState('');
  const [precioSimulado, setPrecioSimulado] = useState('');

  const notificar = (texto: string, esError = false) => {
    setMensaje({ texto, esError });
    setTimeout(() => setMensaje(null), 5000);
  };

  const cargarInstrumentos = useCallback(async () => {
    try {
      const lista = await api.listarInstrumentos();
      setInstrumentos(lista);
      if (lista.length > 0 && !instSeleccionado) {
        setInstSeleccionado(lista[0].id);
        setPrecioSimulado(Number(lista[0].precioActual).toFixed(2));
      }
    } catch (err) {
      notificar(err instanceof Error ? err.message : 'Error cargando instrumentos', true);
    } finally {
      setCargando(false);
    }
  }, [instSeleccionado]);

  useEffect(() => {
    cargarInstrumentos();
  }, [cargarInstrumentos]);

  const seleccionarParaSimular = (inst: InstrumentoResumen) => {
    setInstSeleccionado(inst.id);
    setPrecioSimulado(Number(inst.precioActual).toFixed(2));
  };

  const handleCrearInstrumento = async (e: FormEvent) => {
    e.preventDefault();
    try {
      const creado = await api.crearInstrumento({
        id: nuevoId.trim().toUpperCase(),
        nombre: nuevoNombre.trim(),
        tipo: nuevoTipo,
        precioInicial: Number(nuevoPrecio),
      });
      notificar(`Instrumento ${creado.id} (${creado.nombre}) creado exitosamente.`);
      setNuevoId('');
      setNuevoNombre('');
      setNuevoPrecio('');
      await cargarInstrumentos();
      setTabActiva('mercado');
    } catch (err) {
      notificar(err instanceof Error ? err.message : 'No se pudo crear el instrumento', true);
    }
  };

  const handleActualizarPrecio = async (e: FormEvent) => {
    e.preventDefault();
    if (!instSeleccionado) return;
    try {
      await api.actualizarPrecio(instSeleccionado, Number(precioSimulado));
      notificar(
        `Precio de ${instSeleccionado} actualizado a $${Number(precioSimulado).toFixed(2)}.`
      );
      await cargarInstrumentos();
    } catch (err) {
      notificar(err instanceof Error ? err.message : 'Error al actualizar el precio', true);
    }
  };

  const totalAcciones = instrumentos.filter((i) => i.tipo === 'ACCION').length;
  const totalEtfs = instrumentos.filter((i) => i.tipo === 'ETF').length;

  return (
    <main className="max-w-6xl mx-auto px-6 py-12 space-y-10">
      {/* Notificación superior */}
      {mensaje && (
        <div
          className={`px-5 py-4 rounded-lg border-2 border-black text-sm font-bold flex items-center justify-between ${
            mensaje.esError
              ? 'bg-black text-yellow-400 shadow-[4px_4px_0px_0px_#facc15]'
              : 'bg-yellow-400 text-black shadow-[4px_4px_0px_0px_#000000]'
          }`}
        >
          <span>{mensaje.texto}</span>
          <button
            type="button"
            onClick={() => setMensaje(null)}
            className="text-xs underline ml-6 cursor-pointer"
          >
            Cerrar
          </button>
        </div>
      )}

      {/* 1. Cabecera limpia */}
      <div className="flex flex-wrap items-end justify-between gap-6 pb-6 border-b-2 border-black">
        <div>
          <span className="inline-block bg-black text-yellow-400 text-[11px] font-extrabold uppercase tracking-wider px-2.5 py-1 rounded-sm mb-2">
            Panel de Administración · {sesion.nombreCompleto}
          </span>
          <h1 className="text-3xl font-black tracking-tight text-black">
            Gestión de Mercado
          </h1>
          <p className="text-sm text-neutral-600 mt-1">
            Control de activos listados y simulación de precios en tiempo real
          </p>
        </div>

        {/* Selector de Vista (Pestañas limpias) */}
        <div className="inline-flex bg-white border-2 border-black rounded-lg p-1 shadow-[3px_3px_0px_0px_#000000]">
          <button
            type="button"
            onClick={() => setTabActiva('mercado')}
            className={`px-4 py-2 rounded-md text-xs font-black uppercase tracking-wider transition cursor-pointer ${
              tabActiva === 'mercado'
                ? 'bg-yellow-400 text-black'
                : 'text-neutral-600 hover:text-black'
            }`}
          >
            Cotizaciones y Simulación
          </button>
          <button
            type="button"
            onClick={() => setTabActiva('nuevo')}
            className={`px-4 py-2 rounded-md text-xs font-black uppercase tracking-wider transition cursor-pointer ${
              tabActiva === 'nuevo'
                ? 'bg-black text-yellow-400'
                : 'text-neutral-600 hover:text-black'
            }`}
          >
            + Registrar Instrumento
          </button>
        </div>
      </div>

      {/* 2. Resumen de Activos (3 tarjetas espaciadas) */}
      <section className="grid grid-cols-1 sm:grid-cols-3 gap-6">
        <div className="bg-white border-2 border-black rounded-lg p-5 shadow-[3px_3px_0px_0px_#000000]">
          <p className="text-xs font-bold uppercase tracking-wider text-neutral-500">
            Total Instrumentos
          </p>
          <p className="text-3xl font-mono font-black text-black mt-1">
            {instrumentos.length}
          </p>
        </div>

        <div className="bg-white border-2 border-black rounded-lg p-5 shadow-[3px_3px_0px_0px_#000000]">
          <p className="text-xs font-bold uppercase tracking-wider text-neutral-500">
            Acciones Listadas
          </p>
          <p className="text-3xl font-mono font-black text-black mt-1">{totalAcciones}</p>
        </div>

        <div className="bg-white border-2 border-black rounded-lg p-5 shadow-[3px_3px_0px_0px_#000000]">
          <p className="text-xs font-bold uppercase tracking-wider text-neutral-500">
            ETFs Disponibles
          </p>
          <p className="text-3xl font-mono font-black text-black mt-1">{totalEtfs}</p>
        </div>
      </section>

      {/* 3. Contenido según la pestaña elegida */}
      {tabActiva === 'mercado' ? (
        <div className="space-y-8">
          {/* Barra horizontal ordenada para Simular Precio */}
          <section className="bg-white border-2 border-black rounded-lg p-6 shadow-[4px_4px_0px_0px_#000000] space-y-4">
            <div>
              <h2 className="text-lg font-black text-black">Simular Precio de Mercado</h2>
              <p className="text-xs text-neutral-600">
                Selecciona un activo o haz clic en "Simular" en la tabla para ajustar su cotización
              </p>
            </div>

            <form
              onSubmit={handleActualizarPrecio}
              className="grid grid-cols-1 md:grid-cols-12 gap-4 items-end pt-2"
            >
              <div className="md:col-span-5">
                <label className="block text-xs font-bold uppercase tracking-wider text-black mb-1.5">
                  Instrumento
                </label>
                <select
                  value={instSeleccionado}
                  onChange={(e) => {
                    const id = e.target.value;
                    setInstSeleccionado(id);
                    const encontrado = instrumentos.find((i) => i.id === id);
                    if (encontrado) {
                      setPrecioSimulado(Number(encontrado.precioActual).toFixed(2));
                    }
                  }}
                  className="w-full bg-stone-50 border-2 border-black rounded-lg px-3.5 py-2.5 text-sm font-mono font-bold text-black focus:bg-yellow-50 focus:outline-none"
                >
                  {instrumentos.map((inst) => (
                    <option key={inst.id} value={inst.id}>
                      {inst.id} · {inst.nombre} (${Number(inst.precioActual).toFixed(2)})
                    </option>
                  ))}
                </select>
              </div>

              <div className="md:col-span-4">
                <label className="block text-xs font-bold uppercase tracking-wider text-black mb-1.5">
                  Nuevo Precio ($)
                </label>
                <div className="flex gap-2">
                  <input
                    type="number"
                    step="0.01"
                    min="0.01"
                    required
                    value={precioSimulado}
                    onChange={(e) => setPrecioSimulado(e.target.value)}
                    className="w-full bg-stone-50 border-2 border-black rounded-lg px-3.5 py-2.5 text-sm font-mono font-bold text-black focus:bg-yellow-50 focus:outline-none"
                  />
                  <button
                    type="button"
                    onClick={() =>
                      setPrecioSimulado((prev) => (Number(prev || 0) * 1.05).toFixed(2))
                    }
                    className="px-3 py-2 bg-yellow-100 hover:bg-yellow-200 border-2 border-black rounded-lg font-mono text-xs font-black text-black cursor-pointer"
                  >
                    +5%
                  </button>
                  <button
                    type="button"
                    onClick={() =>
                      setPrecioSimulado((prev) =>
                        Math.max(0.01, Number(prev || 0) * 0.95).toFixed(2)
                      )
                    }
                    className="px-3 py-2 bg-stone-100 hover:bg-stone-200 border-2 border-black rounded-lg font-mono text-xs font-black text-black cursor-pointer"
                  >
                    -5%
                  </button>
                </div>
              </div>

              <div className="md:col-span-3">
                <button
                  type="submit"
                  className="w-full bg-yellow-400 hover:bg-yellow-300 text-black font-black text-sm py-2.5 px-5 rounded-lg border-2 border-black shadow-[3px_3px_0px_0px_#000000] active:translate-x-[1px] active:translate-y-[1px] transition cursor-pointer"
                >
                  Publicar Precio
                </button>
              </div>
            </form>
          </section>

          {/* Tabla Espaciada de Instrumentos */}
          <section className="space-y-3">
            <h2 className="text-lg font-black text-black">Instrumentos Listados</h2>

            <div className="bg-white border-2 border-black rounded-lg overflow-hidden shadow-[4px_4px_0px_0px_#000000]">
              <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse text-sm">
                  <thead>
                    <tr className="bg-black text-white text-xs uppercase tracking-wider">
                      <th className="py-4 px-6 font-bold">Símbolo</th>
                      <th className="py-4 px-6 font-bold">Nombre</th>
                      <th className="py-4 px-6 font-bold">Tipo</th>
                      <th className="py-4 px-6 font-bold text-right">Precio Actual</th>
                      <th className="py-4 px-6 font-bold text-right">Acción</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-neutral-200">
                    {cargando ? (
                      <tr>
                        <td colSpan={5} className="py-10 text-center text-neutral-500">
                          Cargando instrumentos...
                        </td>
                      </tr>
                    ) : (
                      instrumentos.map((inst) => {
                        const seleccionado = inst.id === instSeleccionado;
                        return (
                          <tr
                            key={inst.id}
                            className={`transition ${
                              seleccionado ? 'bg-yellow-50' : 'hover:bg-stone-50'
                            }`}
                          >
                            <td className="py-4 px-6 font-mono font-black text-base text-black">
                              {inst.id}
                            </td>
                            <td className="py-4 px-6 text-neutral-700 font-medium">
                              {inst.nombre}
                            </td>
                            <td className="py-4 px-6">
                              <span className="text-[10px] font-bold uppercase px-2.5 py-1 bg-stone-100 border border-neutral-300 text-neutral-700 rounded">
                                {inst.tipo}
                              </span>
                            </td>
                            <td className="py-4 px-6 text-right font-mono font-black text-base text-black">
                              ${Number(inst.precioActual).toFixed(2)}
                            </td>
                            <td className="py-4 px-6 text-right">
                              <button
                                type="button"
                                onClick={() => seleccionarParaSimular(inst)}
                                className={`text-xs font-bold px-3 py-1.5 rounded border border-black transition cursor-pointer ${
                                  seleccionado
                                    ? 'bg-black text-yellow-400'
                                    : 'bg-white hover:bg-yellow-400 text-black'
                                }`}
                              >
                                {seleccionado ? 'Seleccionado' : 'Simular'}
                              </button>
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
        </div>
      ) : (
        /* Pestaña 2: Formulario Dedicado y Espacioso para Registrar Instrumento */
        <section className="max-w-2xl mx-auto bg-white border-2 border-black rounded-lg p-8 shadow-[4px_4px_0px_0px_#000000] space-y-6">
          <div className="border-b-2 border-black pb-4">
            <h2 className="text-xl font-black text-black">Registrar Nuevo Instrumento</h2>
            <p className="text-sm text-neutral-600 mt-1">
              Completa los datos para listar una nueva acción o ETF en el mercado
            </p>
          </div>

          <form onSubmit={handleCrearInstrumento} className="space-y-5 text-sm">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
              <div>
                <label className="block text-xs font-bold uppercase tracking-wider text-black mb-2">
                  Símbolo (Ticker)
                </label>
                <input
                  type="text"
                  required
                  maxLength={10}
                  value={nuevoId}
                  onChange={(e) => setNuevoId(e.target.value)}
                  placeholder="Ej. TSLA"
                  className="w-full bg-stone-50 border-2 border-black rounded-lg px-4 py-3 font-mono font-bold uppercase text-black focus:bg-yellow-50 focus:outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-bold uppercase tracking-wider text-black mb-2">
                  Tipo de Activo
                </label>
                <select
                  value={nuevoTipo}
                  onChange={(e) => setNuevoTipo(e.target.value as 'ACCION' | 'ETF')}
                  className="w-full bg-stone-50 border-2 border-black rounded-lg px-4 py-3 font-bold text-black focus:bg-yellow-50 focus:outline-none"
                >
                  <option value="ACCION">ACCION</option>
                  <option value="ETF">ETF</option>
                </select>
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-black mb-2">
                Nombre de la Empresa o Fondo
              </label>
              <input
                type="text"
                required
                value={nuevoNombre}
                onChange={(e) => setNuevoNombre(e.target.value)}
                placeholder="Ej. Tesla Inc."
                className="w-full bg-stone-50 border-2 border-black rounded-lg px-4 py-3 text-black focus:bg-yellow-50 focus:outline-none"
              />
            </div>

            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-black mb-2">
                Precio Inicial de Cotización ($)
              </label>
              <input
                type="number"
                step="0.01"
                min="0.01"
                required
                value={nuevoPrecio}
                onChange={(e) => setNuevoPrecio(e.target.value)}
                placeholder="250.00"
                className="w-full bg-stone-50 border-2 border-black rounded-lg px-4 py-3 font-mono font-bold text-black focus:bg-yellow-50 focus:outline-none"
              />
            </div>

            <div className="pt-4 flex items-center justify-end gap-3 border-t border-neutral-200">
              <button
                type="button"
                onClick={() => setTabActiva('mercado')}
                className="bg-white hover:bg-neutral-100 text-black font-bold text-sm px-5 py-2.5 rounded-lg border-2 border-black transition cursor-pointer"
              >
                Cancelar
              </button>
              <button
                type="submit"
                className="bg-yellow-400 hover:bg-yellow-300 text-black font-black text-sm px-6 py-2.5 rounded-lg border-2 border-black shadow-[3px_3px_0px_0px_#000000] active:translate-x-[1px] active:translate-y-[1px] transition cursor-pointer"
              >
                Guardar Instrumento
              </button>
            </div>
          </form>
        </section>
      )}
    </main>
  );
}