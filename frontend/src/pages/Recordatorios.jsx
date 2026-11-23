import { useEffect, useState } from 'react';
import { api, fechaHoraLegible } from '../api.js';
import { useSesion } from '../sesion.jsx';
import Mensaje from '../components/Mensaje.jsx';

// HU_017: recordatorios de las citas de las próximas 24 horas.
export default function Recordatorios() {
  const { token } = useSesion();
  const [recordatorios, setRecordatorios] = useState([]);
  const [error, setError] = useState('');

  const cargar = () => api('/api/recordatorios/mios', { token }).then(setRecordatorios).catch((e) => setError(e.message));
  useEffect(() => { cargar(); }, []);

  const marcarLeido = async (id) => {
    setError('');
    try {
      await api(`/api/recordatorios/${id}/leido`, { metodo: 'PUT', token });
      cargar();
    } catch (e) {
      setError(e.message);
    }
  };

  return (
    <section>
      <h1>Recordatorios</h1>
      <p className="suave">Te avisamos un día antes de cada cita programada.</p>
      <Mensaje error={error} />
      {recordatorios.length === 0 && <p className="suave">No tienes citas en las próximas 24 horas.</p>}
      {recordatorios.map((r) => (
        <article key={r.id} className={`tarjeta recordatorio ${r.leido ? 'leido' : ''}`}>
          <div className="fila">
            <strong>{fechaHoraLegible(r.fechaCita)}</strong>
            <span className="estado">{r.leido ? 'Leído' : 'Nuevo'}</span>
          </div>
          <p>{r.mensaje}</p>
          {!r.leido && (
            <div className="acciones">
              <button className="boton secundario" onClick={() => marcarLeido(r.id)}>Marcar como leído</button>
            </div>
          )}
        </article>
      ))}
    </section>
  );
}
