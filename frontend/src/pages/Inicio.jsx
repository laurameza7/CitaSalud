import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, pesos } from '../api.js';
import Mensaje from '../components/Mensaje.jsx';

// HU_007: especialidades con su tarifa, visibles sin iniciar sesión.
export default function Inicio() {
  const [especialidades, setEspecialidades] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    api('/api/especialidades').then(setEspecialidades).catch((e) => setError(e.message));
  }, []);

  return (
    <section>
      <h1>Especialidades</h1>
      <p className="suave">Elige una especialidad para ver sus médicos y agendar tu cita.</p>
      <Mensaje error={error} />
      {especialidades.map((e) => (
        <Link key={e.id} to={`/especialidades/${e.id}`} className="tarjeta enlace-tarjeta">
          <div className="fila">
            <strong>{e.nombre}</strong>
            <span>{pesos(e.tarifa)}</span>
          </div>
          <p className="suave">{e.descripcion}</p>
        </Link>
      ))}
    </section>
  );
}
