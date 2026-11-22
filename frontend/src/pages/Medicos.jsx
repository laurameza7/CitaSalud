import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api, DIAS, pesos, TIPOS_PACIENTE } from '../api.js';
import { useSesion } from '../sesion.jsx';
import Mensaje from '../components/Mensaje.jsx';

// HU_007: médicos de una especialidad y valor de la cita según el tipo de paciente.
export default function Medicos() {
  const { id } = useParams();
  const { usuario } = useSesion();
  const [especialidad, setEspecialidad] = useState(null);
  const [medicos, setMedicos] = useState([]);
  const [valores, setValores] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    api(`/api/especialidades/${id}`).then(setEspecialidad).catch((e) => setError(e.message));
    api(`/api/medicos?especialidadId=${id}`).then(setMedicos).catch((e) => setError(e.message));
    Promise.all(Object.keys(TIPOS_PACIENTE).map((t) => api(`/api/especialidades/${id}/valor?tipoPaciente=${t}`)))
      .then(setValores).catch(() => {});
  }, [id]);

  const puedeAgendar = !usuario || ['PACIENTE', 'RECEPCIONISTA'].includes(usuario.rol);

  return (
    <section>
      <Link to="/">← Especialidades</Link>
      <h1>{especialidad?.nombre}</h1>
      <Mensaje error={error} />
      {valores.length > 0 && (
        <div className="tarjeta">
          <strong>Valor de la cita</strong>
          {valores.map((v) => (
            <div className="fila" key={v.tipoPaciente}>
              <span>{TIPOS_PACIENTE[v.tipoPaciente]} ({v.porcentajeDescuento} % desc.)</span>
              <span>{pesos(v.total)}</span>
            </div>
          ))}
          <p className="suave">Incluye cargo administrativo de {pesos(valores[0].cargoAdministrativo)}.</p>
        </div>
      )}
      <h2>Médicos</h2>
      {medicos.length === 0 && <p className="suave">No hay médicos activos en esta especialidad.</p>}
      {medicos.map((m) => (
        <article key={m.id} className="tarjeta">
          <strong>{m.nombre}</strong>
          <p className="suave">{m.consultorio} · Registro {m.registroMedico}</p>
          <p className="suave">{m.horarios.map((h) => `${DIAS[h.diaSemana]} ${h.horaInicio.substring(0, 5)}-${h.horaFin.substring(0, 5)}`).join(' · ')}</p>
          {puedeAgendar && <Link className="boton" to={`/agendar/${m.id}`}>Ver horarios y agendar</Link>}
        </article>
      ))}
    </section>
  );
}
