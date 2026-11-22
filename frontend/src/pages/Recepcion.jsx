import { useEffect, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { api, TIPOS_PACIENTE } from '../api.js';
import { useSesion } from '../sesion.jsx';
import Mensaje from '../components/Mensaje.jsx';

// HU_013: la recepcionista busca al paciente por documento y le agenda una cita.
export default function Recepcion() {
  const { token } = useSesion();
  const { state } = useLocation();
  const [documento, setDocumento] = useState('');
  const [paciente, setPaciente] = useState(null);
  const [medicos, setMedicos] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => { api('/api/medicos').then(setMedicos); }, []);

  const buscar = async (e) => {
    e.preventDefault();
    setError(''); setPaciente(null);
    try {
      setPaciente(await api(`/api/pacientes?documento=${encodeURIComponent(documento)}`, { token }));
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <section>
      <h1>Recepción</h1>
      <Mensaje exito={state?.exito} />
      <form onSubmit={buscar} className="fila">
        <input placeholder="Documento del paciente" value={documento} onChange={(e) => setDocumento(e.target.value)} required />
        <button className="boton">Buscar</button>
      </form>
      <Mensaje error={error} />
      {paciente && (
        <>
          <div className="tarjeta">
            <strong>{paciente.nombre}</strong>
            <p className="suave">{paciente.documento} · {paciente.telefono} · {TIPOS_PACIENTE[paciente.tipoPaciente]}</p>
          </div>
          <h2>Elige el médico</h2>
          {medicos.map((m) => (
            <Link key={m.id} className="tarjeta enlace-tarjeta" to={`/agendar/${m.id}?pacienteId=${paciente.id}`}>
              <strong>{m.nombre}</strong> <span className="suave">· {m.especialidad}</span>
            </Link>
          ))}
        </>
      )}
    </section>
  );
}
