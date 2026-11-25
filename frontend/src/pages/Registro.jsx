import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, TIPOS_PACIENTE } from '../api.js';
import { useSesion } from '../sesion.jsx';
import Mensaje from '../components/Mensaje.jsx';

// HU_001: registrarse como paciente.
export default function Registro() {
  const { iniciar } = useSesion();
  const navegar = useNavigate();
  const [datos, setDatos] = useState({
    nombre: '', documento: '', correo: '', telefono: '', fechaNacimiento: '', tipoPaciente: 'PARTICULAR', contrasena: '',
  });
  const [error, setError] = useState('');
  const cambiar = (campo) => (e) => setDatos({ ...datos, [campo]: e.target.value });

  const enviar = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await api('/api/auth/registro', { metodo: 'POST', cuerpo: datos });
      await iniciar(datos.correo, datos.contrasena);
      navegar('/');
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <section className="formulario-tarjeta">
      <h1>Crear cuenta de paciente</h1>
      <p className="suave">Regístrate para agendar y pagar tus citas en línea.</p>
      <form onSubmit={enviar}>
        <label>Nombre completo<input value={datos.nombre} onChange={cambiar('nombre')} required /></label>
        <label>Documento<input value={datos.documento} onChange={cambiar('documento')} required /></label>
        <label>Correo<input type="email" value={datos.correo} onChange={cambiar('correo')} required /></label>
        <label>Teléfono<input value={datos.telefono} onChange={cambiar('telefono')} required /></label>
        <label>Fecha de nacimiento<input type="date" value={datos.fechaNacimiento} onChange={cambiar('fechaNacimiento')} required /></label>
        <label>Tipo de paciente
          <select value={datos.tipoPaciente} onChange={cambiar('tipoPaciente')}>
            {Object.entries(TIPOS_PACIENTE).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
          </select>
        </label>
        <label>Contraseña (mínimo 8 caracteres)<input type="password" value={datos.contrasena} onChange={cambiar('contrasena')} minLength={8} required /></label>
        <Mensaje error={error} />
        <button className="boton">Crear cuenta</button>
      </form>
    </section>
  );
}
