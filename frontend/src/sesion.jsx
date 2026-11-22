import { createContext, useContext, useState } from 'react';
import { api } from './api.js';

// Guarda el token y el usuario que inició sesión, y los comparte con todas las pantallas.
const ContextoSesion = createContext(null);

function leerGuardado() {
  try {
    return JSON.parse(localStorage.getItem('citasalud-sesion')) || null;
  } catch {
    return null;
  }
}

export function ProveedorSesion({ children }) {
  const [sesion, setSesion] = useState(leerGuardado);

  const iniciar = async (correo, contrasena) => {
    const datos = await api('/api/auth/login', { metodo: 'POST', cuerpo: { correo, contrasena } });
    setSesion(datos);
    try { localStorage.setItem('citasalud-sesion', JSON.stringify(datos)); } catch { /* sin almacenamiento */ }
    return datos.usuario;
  };

  const cerrar = () => {
    setSesion(null);
    try { localStorage.removeItem('citasalud-sesion'); } catch { /* sin almacenamiento */ }
  };

  const valor = { token: sesion?.token, usuario: sesion?.usuario, iniciar, cerrar };
  return <ContextoSesion.Provider value={valor}>{children}</ContextoSesion.Provider>;
}

export const useSesion = () => useContext(ContextoSesion);
