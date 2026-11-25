import { Link, NavLink, Navigate, Route, Routes, useNavigate } from 'react-router-dom';
import { useSesion } from './sesion.jsx';
import Logo from './components/Logo.jsx';
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
  VISITANTE: [['/', 'Especialidades']],
  PACIENTE: [['/', 'Especialidades'], ['/mis-citas', 'Mis citas'], ['/recordatorios', 'Recordatorios']],
  MEDICO: [['/agenda', 'Mi agenda'], ['/mi-horario', 'Mi horario']],
  RECEPCIONISTA: [['/recepcion', 'Recepción'], ['/', 'Especialidades']],
  ADMINISTRADOR: [['/admin', 'Administración'], ['/', 'Especialidades']],
};

const NOMBRE_ROL = {
  PACIENTE: 'Paciente', MEDICO: 'Médico', RECEPCIONISTA: 'Recepción', ADMINISTRADOR: 'Administrador',
};

function Protegida({ roles, children }) {
  const { usuario } = useSesion();
  if (!usuario) return <Navigate to="/ingresar" replace />;
  if (!roles.includes(usuario.rol)) return <Navigate to="/" replace />;
  return children;
}

const iniciales = (nombre = '') =>
  nombre.split(' ').filter(Boolean).slice(0, 2).map((p) => p[0].toUpperCase()).join('');

export default function App() {
  const { usuario, cerrar } = useSesion();
  const navegar = useNavigate();
  const rol = usuario?.rol || 'VISITANTE';

  return (
    <div className="app">
      <div className="franja-superior">
        <div className="contenedor">
          <span>Línea de atención 24/7: 601 555 0123</span>
          <span className="oculto-movil">Lun a sáb · 7:00 a. m. – 7:00 p. m.</span>
        </div>
      </div>
      <header className="barra">
        <div className="contenedor barra-interna">
          <Link to="/" className="marca">
            <Logo />
            <span>
              <strong>Cita<span>Salud</span></strong>
              <small>Centro médico especializado</small>
            </span>
          </Link>
          <nav>
            {MENU[rol].map(([ruta, texto]) => (
              <NavLink key={ruta} to={ruta} end>{texto}</NavLink>
            ))}
            {!usuario && (
              <>
                <NavLink to="/ingresar" className="nav-ingresar">Ingresar</NavLink>
                <Link to="/registro" className="boton boton-nav">Crear cuenta</Link>
              </>
            )}
            {usuario && (
              <div className="usuario-chip">
                <span className="avatar">{iniciales(usuario.nombre)}</span>
                <span className="usuario-datos">
                  <strong>{usuario.nombre}</strong>
                  <small>{NOMBRE_ROL[usuario.rol] || usuario.rol}</small>
                </span>
                <button className="enlace" onClick={() => { cerrar(); navegar('/'); }}>Salir</button>
              </div>
            )}
          </nav>
        </div>
      </header>
      <main>
        <div className="contenedor">
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
        </div>
      </main>
      <footer className="pie">
        <div className="contenedor pie-columnas">
          <div>
            <div className="marca marca-pie"><Logo tamano={28} /><strong>Cita<span>Salud</span></strong></div>
            <p>Atención médica especializada con agendamiento y pago en línea.</p>
          </div>
          <div>
            <h4>Contacto</h4>
            <p>Calle 100 # 15-20, Bogotá</p>
            <p>contacto@citasalud.com</p>
          </div>
          <div>
            <h4>Horario</h4>
            <p>Lunes a sábado</p>
            <p>7:00 a. m. – 7:00 p. m.</p>
          </div>
        </div>
        <div className="contenedor pie-legal">© {new Date().getFullYear()} CitaSalud · Proyecto académico</div>
      </footer>
    </div>
  );
}
