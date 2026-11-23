import { NavLink, Navigate, Route, Routes, useNavigate } from 'react-router-dom';
import { useSesion } from './sesion.jsx';
import Inicio from './pages/Inicio.jsx';
import Medicos from './pages/Medicos.jsx';
import Agendar from './pages/Agendar.jsx';
import Login from './pages/Login.jsx';
import Registro from './pages/Registro.jsx';
import MisCitas from './pages/MisCitas.jsx';
import Recordatorios from './pages/Recordatorios.jsx';
import Agenda from './pages/Agenda.jsx';
import MiHorario from './pages/MiHorario.jsx';
import Recepcion from './pages/Recepcion.jsx';
import Admin from './pages/Admin.jsx';

// Menú según el rol (el visitante no tiene cuenta).
const MENU = {
  VISITANTE: [['/', 'Especialidades'], ['/ingresar', 'Ingresar']],
  PACIENTE: [['/', 'Especialidades'], ['/mis-citas', 'Mis citas'], ['/recordatorios', 'Recordatorios']],
  MEDICO: [['/agenda', 'Mi agenda'], ['/mi-horario', 'Mi horario']],
  RECEPCIONISTA: [['/recepcion', 'Recepción'], ['/', 'Especialidades']],
  ADMINISTRADOR: [['/admin', 'Administración'], ['/', 'Especialidades']],
};

function Protegida({ roles, children }) {
  const { usuario } = useSesion();
  if (!usuario) return <Navigate to="/ingresar" replace />;
  if (!roles.includes(usuario.rol)) return <Navigate to="/" replace />;
  return children;
}

export default function App() {
  const { usuario, cerrar } = useSesion();
  const navegar = useNavigate();
  const rol = usuario?.rol || 'VISITANTE';

  return (
    <div className="app">
      <header className="barra">
        <span className="marca">🩺 CitaSalud</span>
        <nav>
          {MENU[rol].map(([ruta, texto]) => (
            <NavLink key={ruta} to={ruta} end>{texto}</NavLink>
          ))}
          {usuario && (
            <button className="enlace" onClick={() => { cerrar(); navegar('/'); }}>Salir</button>
          )}
        </nav>
      </header>
      {usuario && <p className="saludo">Hola, {usuario.nombre}</p>}
      <main>
        <Routes>
          <Route path="/" element={<Inicio />} />
          <Route path="/especialidades/:id" element={<Medicos />} />
          <Route path="/agendar/:medicoId" element={<Agendar />} />
          <Route path="/ingresar" element={<Login />} />
          <Route path="/registro" element={<Registro />} />
          <Route path="/mis-citas" element={<Protegida roles={['PACIENTE']}><MisCitas /></Protegida>} />
          <Route path="/recordatorios" element={<Protegida roles={['PACIENTE']}><Recordatorios /></Protegida>} />
          <Route path="/agenda" element={<Protegida roles={['MEDICO']}><Agenda /></Protegida>} />
          <Route path="/mi-horario" element={<Protegida roles={['MEDICO']}><MiHorario /></Protegida>} />
          <Route path="/recepcion" element={<Protegida roles={['RECEPCIONISTA']}><Recepcion /></Protegida>} />
          <Route path="/admin" element={<Protegida roles={['ADMINISTRADOR']}><Admin /></Protegida>} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </div>
  );
}
