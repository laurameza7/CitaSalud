import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, pesos } from '../api.js';
import { useSesion } from '../sesion.jsx';
import Mensaje from '../components/Mensaje.jsx';

const PASOS = [
  ['1', 'Elige la especialidad', 'Consulta tarifas y médicos disponibles sin necesidad de registrarte.'],
  ['2', 'Escoge día y hora', 'Revisa las franjas libres del especialista en tiempo real.'],
  ['3', 'Paga en línea', 'Confirma tu cita con pago seguro y recibe recordatorios.'],
];

// HU_007: especialidades con su tarifa, visibles sin iniciar sesión.
export default function Inicio() {
  const { usuario } = useSesion();
  const [especialidades, setEspecialidades] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    api('/api/especialidades')
      .then(setEspecialidades)
      .catch((e) => setError(e.message))
      .finally(() => setCargando(false));
  }, []);

  return (
    <>
      {!usuario && (
        <section className="hero">
          <div className="hero-texto">
            <span className="etiqueta">Atención médica especializada</span>
            <h1>Tu salud, a un clic de distancia</h1>
            <p>Agenda, paga y gestiona tus citas con especialistas certificados. Sin filas, sin llamadas y con recordatorios automáticos.</p>
            <div className="acciones">
              <a href="#especialidades" className="boton boton-grande">Agendar una cita</a>
              <Link to="/ingresar" className="boton secundario boton-grande">Ingresar</Link>
            </div>
            <ul className="hero-datos">
              <li><strong>24/7</strong><span>agendamiento en línea</span></li>
              <li><strong>100 %</strong><span>pago digital</span></li>
              <li><strong>3</strong><span>tipos de paciente</span></li>
            </ul>
          </div>
          <div className="hero-visual" aria-hidden="true">
            <div className="hero-tarjeta">
              <span className="estado">Programada</span>
              <strong>Consulta de Cardiología</strong>
              <span className="suave">Jue 28 nov · 9:30 a. m.</span>
              <span className="suave">Consultorio 204</span>
              <div className="hero-confirmado">✓ Pago confirmado</div>
            </div>
            <div className="hero-burbuja">🔔 Te recordaremos 24 h antes</div>
          </div>
        </section>
      )}

      {!usuario && (
        <section className="pasos">
          {PASOS.map(([n, titulo, texto]) => (
            <div key={n} className="paso">
              <span className="paso-numero">{n}</span>
              <div><strong>{titulo}</strong><p className="suave">{texto}</p></div>
            </div>
          ))}
        </section>
      )}

      <section id="especialidades">
        <div className="encabezado">
          <div>
            <h1>Especialidades</h1>
            <p className="suave">Elige una especialidad para ver sus médicos y agendar tu cita.</p>
          </div>
        </div>
        <Mensaje error={error} />
        {cargando && <p className="suave cargando">Cargando especialidades…</p>}
        <div className="rejilla">
          {especialidades.map((e) => (
            <Link key={e.id} to={`/especialidades/${e.id}`} className="tarjeta enlace-tarjeta especialidad">
              <span className="especialidad-icono">{e.nombre?.[0]}</span>
              <strong>{e.nombre}</strong>
              <p className="suave">{e.descripcion}</p>
              <div className="fila especialidad-pie">
                <span className="suave">Desde</span>
                <span className="precio">{pesos(e.tarifa)}</span>
              </div>
              <span className="ver-mas">Ver médicos →</span>
            </Link>
          ))}
        </div>
      </section>
    </>
  );
}
