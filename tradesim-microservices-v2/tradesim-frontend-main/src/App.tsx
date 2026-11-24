import { useState } from 'react';
import { AdminDashboard } from './components/AdminDashboard';
import { LoginView } from './components/LoginView';
import { MercadoView } from './components/MercadoView';
import { Navbar, type PestanaInversionista } from './components/Navbar';
import { OrdenesView } from './components/OrdenesView';
import { PortafolioView } from './components/PortafolioView';
import type { UsuarioSesion } from './types/trading';

const STORAGE_KEY = 'g1_trading_sesion';

export default function App() {
  const [sesion, setSesion] = useState<UsuarioSesion | null>(() => {
    const guardada = localStorage.getItem(STORAGE_KEY);
    return guardada ? (JSON.parse(guardada) as UsuarioSesion) : null;
  });

  const [pestana, setPestana] = useState<PestanaInversionista>('mercado');

  const handleLoginSuccess = (nuevaSesion: UsuarioSesion) => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(nuevaSesion));
    setSesion(nuevaSesion);
    setPestana('mercado');
  };

  const handleLogout = () => {
    localStorage.removeItem(STORAGE_KEY);
    setSesion(null);
  };

  if (!sesion) {
    return <LoginView onLoginSuccess={handleLoginSuccess} />;
  }

  const inversionistaId = sesion.inversionistaId ?? 'INV-01';

  return (
    <div className="min-h-screen bg-stone-50 text-black font-sans">
      <Navbar
        sesion={sesion}
        pestanaActiva={pestana}
        onCambiarPestana={setPestana}
        onLogout={handleLogout}
      />

      {sesion.rol === 'ADMIN' ? (
        <AdminDashboard sesion={sesion} />
      ) : pestana === 'mercado' ? (
        <MercadoView inversionistaId={inversionistaId} />
      ) : pestana === 'ordenes' ? (
        <OrdenesView inversionistaId={inversionistaId} />
      ) : (
        <PortafolioView
          inversionistaId={inversionistaId}
          onIrAOrdenes={() => setPestana('ordenes')}
        />
      )}
    </div>
  );
}