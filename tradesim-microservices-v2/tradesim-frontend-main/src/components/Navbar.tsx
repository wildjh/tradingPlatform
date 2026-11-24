import type { UsuarioSesion } from '../types/trading';

export type PestanaInversionista = 'mercado' | 'portafolio' | 'ordenes';

interface NavbarProps {
  sesion: UsuarioSesion;
  pestanaActiva: PestanaInversionista;
  onCambiarPestana: (pestana: PestanaInversionista) => void;
  onLogout: () => void;
}

export function Navbar({
  sesion,
  pestanaActiva,
  onCambiarPestana,
  onLogout,
}: NavbarProps) {
  const tabs: { id: PestanaInversionista; label: string }[] = [
    { id: 'mercado', label: 'Mercado' },
    { id: 'portafolio', label: 'Portafolio' },
    { id: 'ordenes', label: 'Órdenes' },
  ];

  return (
    <header className="bg-black text-white border-b-2 border-yellow-400">
      <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
        {/* Izquierda: Nombre de la aplicación */}
        <div className="flex items-center gap-2.5">
          <span className="inline-block w-3 h-3 rounded-sm bg-yellow-400" />
          <span className="text-xl font-black tracking-tight lowercase text-white">
            trade<span className="text-yellow-400">sim</span>
          </span>
        </div>

        {/* Derecha: Opciones de navegación + Sesión */}
        <div className="flex items-center gap-8">
          {sesion.rol !== 'ADMIN' && (
            <nav className="flex items-center gap-6">
              {tabs.map((tab) => {
                const activa = pestanaActiva === tab.id;
                return (
                  <button
                    key={tab.id}
                    type="button"
                    onClick={() => onCambiarPestana(tab.id)}
                    className={`text-sm font-semibold py-1 border-b-2 transition cursor-pointer ${
                      activa
                        ? 'text-yellow-400 border-yellow-400'
                        : 'text-neutral-300 border-transparent hover:text-white'
                    }`}
                  >
                    {tab.label}
                  </button>
                );
              })}
            </nav>
          )}

          <div className="flex items-center gap-3 pl-6 border-l border-neutral-800">
            <span className="text-xs text-neutral-300 hidden sm:inline">
              {sesion.nombreCompleto}
            </span>
            <button
              type="button"
              onClick={onLogout}
              className="bg-yellow-400 hover:bg-yellow-300 text-black font-bold text-xs px-3 py-1.5 rounded transition cursor-pointer"
            >
              Salir
            </button>
          </div>
        </div>
      </div>
    </header>
  );
}