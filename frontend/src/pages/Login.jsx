import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useSesion } from '../sesion.jsx';
import Mensaje from '../components/Mensaje.jsx';

const INICIO_POR_ROL = { PACIENTE: '/mis-citas', MEDICO: '/agenda', RECEPCIONISTA: '/recepcion', ADMINISTRADOR: '/admin' };

// HU_002: iniciar sesión.
export default function Login() {
  const { iniciar } = useSesion();
  const navegar = useNavigate();
  const [correo, setCorreo] = useState('');
  const [contrasena, setContrasena] = useState('');
  const [error, setError] = useState('');

  const enviar = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const usuario = await iniciar(correo, contrasena);
      navegar(INICIO_POR_ROL[usuario.rol] || '/');
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <section>
      <h1>Ingresar</h1>
      <form onSubmit={enviar}>
        <label>Correo<input type="email" value={correo} onChange={(e) => setCorreo(e.target.value)} required /></label>
        <label>Contraseña<input type="password" value={contrasena} onChange={(e) => setContrasena(e.target.value)} required /></label>
        <Mensaje error={error} />
        <button className="boton">Ingresar</button>
      </form>
      <p>¿No tienes cuenta? <Link to="/registro">Regístrate como paciente</Link></p>
    </section>
  );
}
