import { ESTADOS, fechaHoraLegible, pesos } from '../api.js';

// Tarjeta con los datos de una cita. Los botones de acción llegan como hijos.
export default function TarjetaCita({ cita, mostrarPaciente, children }) {
  return (
    <article className="tarjeta">
      <div className="fila">
        <strong>{fechaHoraLegible(cita.fechaHora)}</strong>
        <span className={`estado ${cita.estado}`}>{ESTADOS[cita.estado]}</span>
      </div>
      <p>{cita.especialidad} · {cita.medico} · {cita.consultorio}</p>
      {mostrarPaciente && <p>Paciente: {cita.paciente} ({cita.documentoPaciente})</p>}
      {cita.motivo && <p className="suave">Motivo: {cita.motivo}</p>}
      <p className="suave">
        Pagado {pesos(cita.valorPagado)}{cita.reembolsada ? ' · reembolsado' : ''}
        {cita.reprogramaciones > 0 ? ` · reprogramada ${cita.reprogramaciones} ${cita.reprogramaciones === 1 ? 'vez' : 'veces'}` : ''}
      </p>
      {cita.notasAtencion && <p className="suave">Notas: {cita.notasAtencion}</p>}
      {children && <div className="acciones">{children}</div>}
    </article>
  );
}
