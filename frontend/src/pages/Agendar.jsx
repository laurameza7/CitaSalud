import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { api, pesos } from '../api.js';
import { useSesion } from '../sesion.jsx';
import Mensaje from '../components/Mensaje.jsx';
import SelectorFranja from '../components/SelectorFranja.jsx';

// HU_009 (paciente) y HU_013 (recepcionista con ?pacienteId=...): agendar y pagar una cita.
export default function Agendar() {
  const { medicoId } = useParams();
  const [parametros] = useSearchParams();
  const pacienteId = parametros.get('pacienteId');
  const { usuario, token } = useSesion();
  const navegar = useNavigate();
  const [medico, setMedico] = useState(null);
  const [fechaHora, setFechaHora] = useState(null);
  const [motivo, setMotivo] = useState('');
  const [tarjeta, setTarjeta] = useState('');
  const [valor, setValor] = useState(null);
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    api(`/api/medicos/${medicoId}`).then(setMedico).catch((e) => setError(e.message));
  }, [medicoId]);

  useEffect(() => {
    if (medico && usuario?.tipoPaciente) {
      api(`/api/especialidades/${medico.especialidadId}/valor?tipoPaciente=${usuario.tipoPaciente}`).then(setValor);
    }
  }, [medico, usuario]);

  const agendar = async (e) => {
    e.preventDefault();
    setError('');
    setEnviando(true);
    try {
      await api('/api/citas', {
        metodo: 'POST', token,
        cuerpo: { medicoId, fechaHora, motivo, numeroTarjeta: tarjeta, pacienteId },
      });
      navegar(usuario.rol === 'PACIENTE' ? '/mis-citas' : '/recepcion', { state: { exito: 'Cita agendada y pagada' } });
    } catch (err) {
      setError(err.message);
    } finally {
      setEnviando(false);
    }
  };

  if (!usuario) {
    return (
      <section>
        <h1>Agendar cita</h1>
        <p>Para agendar necesitas una cuenta de paciente.</p>
        <Link className="boton" to="/ingresar">Ingresar</Link> <Link to="/registro">Crear cuenta</Link>
      </section>
    );
  }

  return (
    <section className="formulario-tarjeta">
      <h1>Agendar cita</h1>
      {medico && <p><strong>{medico.nombre}</strong> · {medico.especialidad} · {medico.consultorio}</p>}
      {pacienteId && <p className="suave">Agendando en nombre de un paciente (recepción).</p>}
      <form onSubmit={agendar}>
        <SelectorFranja medicoId={medicoId} valor={fechaHora} alCambiar={setFechaHora} />
        <label>Motivo de la consulta
          <input value={motivo} onChange={(e) => setMotivo(e.target.value)} placeholder="Opcional" />
        </label>
        <label>Número de tarjeta (16 dígitos)
          <input value={tarjeta} onChange={(e) => setTarjeta(e.target.value)} inputMode="numeric" maxLength={19}
                 placeholder="4111 1111 1111 1111" required />
        </label>
        <p className="suave">Pasarela simulada: se rechazan las tarjetas que terminan en 0000.</p>
        {valor && <p className="total">Total a pagar: {pesos(valor.total)}</p>}
        <Mensaje error={error} />
        <button className="boton" disabled={!fechaHora || enviando}>
          {enviando ? 'Procesando pago…' : 'Pagar y agendar'}
        </button>
      </form>
    </section>
  );
}
