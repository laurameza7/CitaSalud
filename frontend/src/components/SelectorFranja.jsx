import { useEffect, useState } from 'react';
import { api, hoyISO } from '../api.js';

// Elige una fecha y muestra las franjas libres del médico (HU_008).
export default function SelectorFranja({ medicoId, valor, alCambiar }) {
  const [fecha, setFecha] = useState(hoyISO());
  const [franjas, setFranjas] = useState([]);
  const [cargando, setCargando] = useState(false);

  useEffect(() => {
    setCargando(true);
    alCambiar(null);
    api(`/api/medicos/${medicoId}/disponibilidad?fecha=${fecha}`)
      .then((d) => setFranjas(d.franjasLibres))
      .catch(() => setFranjas([]))
      .finally(() => setCargando(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [medicoId, fecha]);

  return (
    <div>
      <label>Fecha
        <input type="date" value={fecha} min={hoyISO()} onChange={(e) => setFecha(e.target.value)} />
      </label>
      {cargando && <p className="suave">Buscando franjas…</p>}
      {!cargando && franjas.length === 0 && <p className="suave">No hay franjas libres ese día.</p>}
      <div className="franjas">
        {franjas.map((h) => {
          const fechaHora = `${fecha}T${h.substring(0, 5)}`;
          return (
            <button key={h} type="button" className={valor === fechaHora ? 'franja activa' : 'franja'}
                    onClick={() => alCambiar(fechaHora)}>
              {h.substring(0, 5)}
            </button>
          );
        })}
      </div>
    </div>
  );
}
