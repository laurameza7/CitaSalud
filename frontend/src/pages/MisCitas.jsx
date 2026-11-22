import { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { api } from '../api.js';
import { useSesion } from '../sesion.jsx';
import Mensaje from '../components/Mensaje.jsx';
import SelectorFranja from '../components/SelectorFranja.jsx';
import TarjetaCita from '../components/TarjetaCita.jsx';

// HU_010, HU_011 y HU_012: ver, cancelar y reprogramar mis citas.
export default function MisCitas() {
  const { token } = useSesion();
  const { state } = useLocation();
  const [citas, setCitas] = useState([]);
  const [error, setError] = useState('');
  const [exito, setExito] = useState(state?.exito || '');
  const [reprogramando, setReprogramando] = useState(null);
  const [nuevaFecha, setNuevaFecha] = useState(null);

  const cargar = () => api('/api/citas/mias', { token }).then(setCitas).catch((e) => setError(e.message));
  useEffect(() => { cargar(); }, []);

  const accion = async (promesa, mensaje) => {
    setError(''); setExito('');
    try {
      await promesa;
      setExito(mensaje);
      setReprogramando(null);
      cargar();
    } catch (e) {
      setError(e.message);
    }
  };

  const cancelar = (cita) => {
    if (window.confirm('¿Cancelar esta cita? Se solicitará el reembolso.')) {
      accion(api(`/api/citas/${cita.id}/cancelar`, { metodo: 'PUT', token }), 'Cita cancelada y reembolso solicitado');
    }
  };

  const reprogramar = (cita) =>
    accion(api(`/api/citas/${cita.id}/reprogramar`, { metodo: 'PUT', token, cuerpo: { nuevaFechaHora: nuevaFecha } }),
      'Cita reprogramada');

  return (
    <section>
      <h1>Mis citas</h1>
      <Mensaje error={error} exito={exito} />
      {citas.length === 0 && <p className="suave">Aún no tienes citas.</p>}
      {citas.map((c) => (
        <TarjetaCita key={c.id} cita={c}>
          {c.estado === 'PROGRAMADA' && (
            <>
              <button className="boton secundario" onClick={() => { setReprogramando(c.id); setNuevaFecha(null); }}>Reprogramar</button>
              <button className="boton peligro" onClick={() => cancelar(c)}>Cancelar</button>
            </>
          )}
          {reprogramando === c.id && (
            <div className="panel">
              <SelectorFranja medicoId={c.medicoId} valor={nuevaFecha} alCambiar={setNuevaFecha} />
              <button className="boton" disabled={!nuevaFecha} onClick={() => reprogramar(c)}>Confirmar nueva hora</button>
            </div>
          )}
        </TarjetaCita>
      ))}
    </section>
  );
}
