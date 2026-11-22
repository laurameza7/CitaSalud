import { useEffect, useState } from 'react';
import { api, DIAS } from '../api.js';
import { useSesion } from '../sesion.jsx';
import Mensaje from '../components/Mensaje.jsx';

// HU_006: el médico define los días y horas en que atiende.
export default function MiHorario() {
  const { token, usuario } = useSesion();
  const [horarios, setHorarios] = useState([]);
  const [error, setError] = useState('');
  const [exito, setExito] = useState('');

  useEffect(() => {
    api(`/api/medicos/${usuario.id}`).then((m) => setHorarios(m.horarios.map((h) => ({
      ...h, horaInicio: h.horaInicio.substring(0, 5), horaFin: h.horaFin.substring(0, 5),
    })))).catch((e) => setError(e.message));
  }, [usuario.id]);

  const cambiar = (i, campo, valor) => setHorarios(horarios.map((h, j) => (i === j ? { ...h, [campo]: valor } : h)));
  const agregar = () => setHorarios([...horarios, { diaSemana: 'MONDAY', horaInicio: '08:00', horaFin: '12:00' }]);
  const quitar = (i) => setHorarios(horarios.filter((_, j) => j !== i));

  const guardar = async () => {
    setError(''); setExito('');
    try {
      await api(`/api/medicos/${usuario.id}/horarios`, { metodo: 'PUT', token, cuerpo: horarios });
      setExito('Horario guardado');
    } catch (e) {
      setError(e.message);
    }
  };

  return (
    <section>
      <h1>Mi horario de atención</h1>
      <p className="suave">Las citas duran 30 minutos. Los pacientes solo pueden agendar dentro de estos bloques.</p>
      {horarios.map((h, i) => (
        <div className="fila tarjeta" key={i}>
          <select value={h.diaSemana} onChange={(e) => cambiar(i, 'diaSemana', e.target.value)}>
            {Object.entries(DIAS).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
          </select>
          <input type="time" step="1800" value={h.horaInicio} onChange={(e) => cambiar(i, 'horaInicio', e.target.value)} />
          <input type="time" step="1800" value={h.horaFin} onChange={(e) => cambiar(i, 'horaFin', e.target.value)} />
          <button className="boton peligro" onClick={() => quitar(i)}>✕</button>
        </div>
      ))}
      <button className="boton secundario" onClick={agregar}>Agregar bloque</button>{' '}
      <button className="boton" onClick={guardar}>Guardar horario</button>
      <Mensaje error={error} exito={exito} />
    </section>
  );
}
