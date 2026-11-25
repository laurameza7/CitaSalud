import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useSesion } from '../sesion.jsx';
import Mensaje from '../components/Mensaje.jsx';
import Logo from '../components/Logo.jsx';

const INICIO_POR_ROL = { PACIENTE: '/mis-citas', MEDICO: '/agenda', RECEPCIONISTA: '/recepcion', ADMINISTRADOR: '/admin' };

// Cuentas de demostración para entrar con un clic durante la presentación.
const CUENTAS_PRUEBA = [
  { rol: 'Administrador', icono: '🛡️', correo: 'admin@citasalud.com', contrasena: 'Admin123*' },
  { rol: 'Recepción', icono: '🗂️', correo: 'recepcion@citasalud.com', contrasena: 'Recepcion123*' },
  { rol: 'Médico', icono: '🩺', correo: 'carlos.ruiz@citasalud.com', contrasena: 'Medico123*' },
  { rol: 'Paciente', icono: '🙂', correo: 'ana.perez@correo.com', contrasena: 'Paciente123*' },
];

// HU_002: iniciar sesión.
export default function Login() {
  const { iniciar } = useSesion();
  const navegar = useNavigate();
  const [correo, setCorreo] = useState('');
  const [contrasena, setContrasena] = useState('');
  const [error, setError] = useState('');
  const [cargando, setCargando] = useState(null); // 'form' o el correo de la cuenta de prueba

  const entrar = async (c, p, origen) => {
    setError('');
    setCargando(origen);
    try {
      const usuario = await iniciar(c, p);
      navegar(INICIO_POR_ROL[usuario.rol] || '/');
    } catch (err) {
      setError(err.message === 'Failed to fetch'
        ? 'No fue posible conectar con el servidor. Intenta de nuevo en unos segundos.'
        : err.message);
    } finally {
      setCargando(null);
    }
  };

  const enviar = (e) => {
    e.preventDefault();
    entrar(correo, contrasena, 'form');
  };

  const usarCuenta = (cuenta) => {
    setCorreo(cuenta.correo);
    setContrasena(cuenta.contrasena);
    entrar(cuenta.correo, cuenta.contrasena, cuenta.correo);
  };

  return (
    <section className="acceso">
      <div className="acceso-lateral">
        <Logo tamano={48} />
        <h2>Bienvenido a CitaSalud</h2>
        <p>Gestiona tus citas, consulta tu agenda y recibe recordatorios desde un solo lugar.</p>
        <ul>
          <li>✓ Agendamiento y pago en línea</li>
          <li>✓ Reprogramación y cancelación con reembolso</li>
          <li>✓ Recordatorios automáticos de tus citas</li>
        </ul>
      </div>

      <div className="acceso-formulario">
        <h1>Ingresar</h1>
        <p className="suave">Accede con tu correo y contraseña.</p>
        <form onSubmit={enviar}>
          <label>Correo<input type="email" value={correo} onChange={(e) => setCorreo(e.target.value)} placeholder="tucorreo@ejemplo.com" required /></label>
          <label>Contraseña<input type="password" value={contrasena} onChange={(e) => setContrasena(e.target.value)} placeholder="••••••••" required /></label>
          <Mensaje error={error} />
          <button className="boton boton-grande" disabled={!!cargando}>
            {cargando === 'form' ? 'Ingresando…' : 'Ingresar'}
          </button>
        </form>
        <p className="suave">¿No tienes cuenta? <Link to="/registro">Regístrate como paciente</Link></p>

        <div className="cuentas-prueba">
          <div className="separador"><span>Cuentas de prueba</span></div>
          <p className="suave">Entra con un clic seleccionando un rol.</p>
          <div className="cuentas-rejilla">
            {CUENTAS_PRUEBA.map((c) => (
              <button key={c.correo} type="button" className="cuenta" disabled={!!cargando} onClick={() => usarCuenta(c)}>
                <span className="cuenta-icono">{c.icono}</span>
                <span className="cuenta-texto">
                  <strong>{cargando === c.correo ? 'Ingresando…' : c.rol}</strong>
                  <small>{c.correo}</small>
                </span>
              </button>
            ))}
          </div>
          {cargando && cargando !== 'form' && (
            <p className="suave aviso">Si el servidor estaba inactivo, la primera conexión puede tardar hasta un minuto.</p>
          )}
        </div>
      </div>
    </section>
  );
}
