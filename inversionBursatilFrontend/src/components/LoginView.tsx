import { useState, type FormEvent } from 'react';
import { api } from '../services/api';
import type { UsuarioSesion } from '../types/trading';

interface LoginViewProps {
  onLoginSuccess: (sesion: UsuarioSesion) => void;
}

const CUENTAS_DEMO = [
  { username: 'inv1', pass: '1234', rol: 'Inversionista', detalle: 'INV-01 · AAPL / MSFT' },
  { username: 'inv2', pass: '1234', rol: 'Inversionista', detalle: 'INV-02 · SPY' },
  { username: 'inv3', pass: '1234', rol: 'Inversionista', detalle: 'INV-03 · Nuevo' },
  { username: 'admin', pass: 'admin123', rol: 'Administrador', detalle: 'Gestión de Mercado' },
];

export function LoginView({ onLoginSuccess }: LoginViewProps) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const autenticar = async (user: string, pass: string) => {
    setError(null);
    setCargando(true);
    try {
      const sesion = await api.login(user, pass);
      onLoginSuccess(sesion);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo iniciar sesión');
    } finally {
      setCargando(false);
    }
  };

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    autenticar(username, password);
  };

  return (
    <div className="min-h-screen bg-white text-black flex items-center justify-center p-6">
      <div className="w-full max-w-md space-y-6">
        {/* Cabecera */}
        <div className="text-center space-y-2">
          <div className="inline-flex items-center justify-center px-3 py-1.5 rounded bg-black text-white font-black text-xl tracking-tight lowercase">
            trade<span className="text-yellow-400">sim</span>
          </div>
          <h1 className="text-2xl font-black tracking-tight text-black">
            Iniciar Sesión
          </h1>
          <p className="text-sm text-neutral-600">
            Ingresa como inversionista o administrador del mercado
          </p>
        </div>

        {/* Formulario Principal */}
        <div className="bg-white border-2 border-black rounded-xl p-6 shadow-[4px_4px_0px_0px_#000000] space-y-4">
          {error && (
            <div className="bg-black text-yellow-400 border-l-4 border-yellow-400 text-sm px-4 py-2.5 font-medium">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-black mb-1.5">
                Usuario
              </label>
              <input
                type="text"
                required
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Ej. inv1 o admin"
                className="w-full bg-white border-2 border-neutral-300 focus:border-black focus:outline-none rounded-lg px-3.5 py-2.5 text-sm text-black"
              />
            </div>

            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-black mb-1.5">
                Contraseña
              </label>
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full bg-white border-2 border-neutral-300 focus:border-black focus:outline-none rounded-lg px-3.5 py-2.5 text-sm text-black"
              />
            </div>

            <button
              type="submit"
              disabled={cargando}
              className="w-full bg-yellow-400 hover:bg-yellow-300 border-2 border-black disabled:opacity-50 text-black font-black py-2.5 rounded-lg text-sm transition cursor-pointer"
            >
              {cargando ? 'Verificando...' : 'Ingresar a tradesim'}
            </button>
          </form>
        </div>

        {/* Accesos Rápidos Precargados */}
        <div className="bg-neutral-50 border-2 border-black rounded-xl p-4 space-y-3">
          <p className="text-xs font-bold uppercase tracking-wider text-black text-center">
            Acceso rápido · Cuentas precargadas
          </p>
          <div className="grid grid-cols-2 gap-2.5">
            {CUENTAS_DEMO.map((cuenta) => (
              <button
                key={cuenta.username}
                type="button"
                disabled={cargando}
                onClick={() => {
                  setUsername(cuenta.username);
                  setPassword(cuenta.pass);
                  autenticar(cuenta.username, cuenta.pass);
                }}
                className="text-left bg-white hover:bg-yellow-50 border border-black rounded-lg p-2.5 transition cursor-pointer"
              >
                <div className="flex items-center justify-between">
                  <span className="font-mono font-bold text-xs text-black">
                    {cuenta.username}
                  </span>
                  <span
                    className={`text-[10px] px-1.5 py-0.5 rounded font-bold ${
                      cuenta.rol === 'Administrador'
                        ? 'bg-black text-yellow-400'
                        : 'bg-yellow-400 text-black'
                    }`}
                  >
                    {cuenta.rol}
                  </span>
                </div>
                <p className="text-[11px] text-neutral-600 mt-1 truncate">
                  {cuenta.detalle}
                </p>
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}