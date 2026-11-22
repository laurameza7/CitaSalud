import { useEffect, useState } from 'react';
import { api, hoyISO } from '../api.js';
import { useSesion } from '../sesion.jsx';
import Mensaje from '../components/Mensaje.jsx';
import TarjetaCita from '../components/TarjetaCita.jsx';

// HU_014 y HU_015: agenda del médico y registro de atención o inasistencia.
export default function Agenda() {
  const { token } = useSesion();
  const [fecha, setFecha] = useState(hoyISO());
  const [citas, setCitas] = useState([]);
  const [notas, setNotas] = useState({});
  const [error, setError] = useState('');
  const [exito, setExito] = useState('');

  const cargar = () => api(`/api/citas/agenda?fecha=${fecha}`, { token }).then(setCitas).catch((e) => setError(e.message));
  useEffect(() => { cargar(); }, [fecha]);

  const accion = async (ruta, cuerpo, mensaje) => {
    setError(''); setExito('');
    try {
      await api(ruta, { metodo: 'PUT', token, cuerpo });
      setExito(mensaje);
      cargar();
    } catch (e) {
      setError(e.message);
    }
  };

  return (
    <section>
      <h1>Mi agenda</h1>
      <label>Fecha<input type="date" value={fecha} onChange={(e) => setFecha(e.target.value)} /></label>
      <Mensaje error={error} exito={exito} />
      {citas.length === 0 && <p className="suave">No tienes citas este día.</p>}
      {citas.map((c) => (
        <TarjetaCita key={c.id} cita={c} mostrarPaciente>
          {c.estado === 'PROGRAMADA' && (
            <div className="panel">
              <textarea placeholder="Notas de la consulta" value={notas[c.id] || ''}
                        onChange={(e) => setNotas({ ...notas, [c.id]: e.target.value })} />
              <button className="boton" onClick={() => accion(`/api/citas/${c.id}/atencion`, { notas: notas[c.id] || '' }, 'Atención registrada')}>
                Registrar atención
              </button>
              <button className="boton secundario" onClick={() => accion(`/api/citas/${c.id}/inasistencia`, undefined, 'Inasistencia registrada')}>
                No asistió
              </button>
            </div>
          )}
        </TarjetaCita>
      ))}
    </section>
  );
}
