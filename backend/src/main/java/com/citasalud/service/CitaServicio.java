package com.citasalud.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.citasalud.dto.AgendarCitaRequest;
import com.citasalud.dto.CitaResponse;
import com.citasalud.exception.AccesoDenegadoException;
import com.citasalud.exception.ConflictoException;
import com.citasalud.exception.PagoRechazadoException;
import com.citasalud.exception.RecursoNoEncontradoException;
import com.citasalud.exception.ReglaNegocioException;
import com.citasalud.integracion.PasarelaPagos;
import com.citasalud.integracion.ResultadoPago;
import com.citasalud.model.Cita;
import com.citasalud.model.EstadoCita;
import com.citasalud.model.Medico;
import com.citasalud.model.Paciente;
import com.citasalud.model.Rol;
import com.citasalud.repository.CitaRepositorio;
import com.citasalud.security.UsuarioActual;

/**
 * Lógica de las citas: agendar, consultar, cancelar, reprogramar y atender
 * (HU_009 a HU_015). Coordina el dominio, los repositorios y la pasarela de pagos.
 */
@Service
public class CitaServicio {

    private final CitaRepositorio citas;
    private final MedicoServicio medicoServicio;
    private final PacienteServicio pacienteServicio;
    private final DisponibilidadServicio disponibilidad;
    private final PasarelaPagos pasarela;
    private final Clock reloj;

    public CitaServicio(CitaRepositorio citas, MedicoServicio medicoServicio, PacienteServicio pacienteServicio,
                        DisponibilidadServicio disponibilidad, PasarelaPagos pasarela, Clock reloj) {
        this.citas = citas;
        this.medicoServicio = medicoServicio;
        this.pacienteServicio = pacienteServicio;
        this.disponibilidad = disponibilidad;
        this.pasarela = pasarela;
        this.reloj = reloj;
    }

    /** HU_009 y HU_013: agenda la cita solo si la franja está libre y el pago se aprueba. */
    public CitaResponse agendar(AgendarCitaRequest datos, UsuarioActual actual) {
        Paciente paciente = pacienteDeLaPeticion(datos.pacienteId(), actual);
        Medico medico = medicoServicio.buscarMedico(datos.medicoId());
        if (!medico.isActivo()) {
            throw new ReglaNegocioException("El médico no está disponible para agendar citas");
        }
        LocalDateTime ahora = ahora();
        Cita cita = medico.crearCita(paciente, datos.fechaHora(), datos.motivo(), ahora);

        disponibilidad.verificarFranjaLibre(medico, cita.getFechaHora());
        verificarPacienteSinCruce(paciente, cita.getFechaHora());
        if (citas.existsByPacienteAndEspecialidadAndEstado(paciente, medico.getEspecialidad(), EstadoCita.PROGRAMADA)) {
            throw new ConflictoException("Ya tienes una cita programada en " + medico.getEspecialidad().getNombre());
        }

        ResultadoPago pago = pasarela.cobrar(datos.numeroTarjeta(), cita.getValorPagado(),
                "Cita de " + medico.getEspecialidad().getNombre());
        if (!pago.aprobado()) {
            throw new PagoRechazadoException("Pago rechazado: " + pago.mensaje());
        }
        cita.registrarPago(pago.referencia());
        return CitaResponse.desde(citas.save(cita));
    }

    /** HU_010 */
    public List<CitaResponse> misCitas(UsuarioActual actual) {
        Paciente paciente = pacienteServicio.buscarPaciente(actual.id());
        return citas.findByPacienteOrderByFechaHoraDesc(paciente).stream().map(CitaResponse::desde).toList();
    }

    public CitaResponse obtener(String id, UsuarioActual actual) {
        Cita cita = buscarCita(id);
        boolean puedeVer = switch (actual.rol()) {
            case PACIENTE -> cita.esDelPaciente(actual.id());
            case MEDICO -> cita.esDelMedico(actual.id());
            case RECEPCIONISTA, ADMINISTRADOR -> true;
        };
        if (!puedeVer) {
            throw new AccesoDenegadoException("No puedes ver una cita que no es tuya");
        }
        return CitaResponse.desde(cita);
    }

    /** HU_011: cancela y pide el reembolso (RN_08, RN_10). */
    public CitaResponse cancelar(String id, UsuarioActual actual) {
        Cita cita = buscarCita(id);
        verificarPuedeModificar(cita, actual);
        cita.cancelar(ahora());
        if (pasarela.reembolsar(cita.getReferenciaPago(), cita.getValorPagado())) {
            cita.marcarReembolsada();
        }
        return CitaResponse.desde(citas.save(cita));
    }

    /** HU_012: mueve la cita a otra franja libre del mismo médico (RN_08, RN_11). */
    public CitaResponse reprogramar(String id, LocalDateTime nuevaFechaHora, UsuarioActual actual) {
        Cita cita = buscarCita(id);
        verificarPuedeModificar(cita, actual);
        if (nuevaFechaHora.equals(cita.getFechaHora())) {
            throw new ReglaNegocioException("La nueva fecha es igual a la actual");
        }
        disponibilidad.verificarFranjaLibre(cita.getMedico(), nuevaFechaHora);
        verificarPacienteSinCruce(cita.getPaciente(), nuevaFechaHora);
        cita.reprogramar(nuevaFechaHora, ahora());
        return CitaResponse.desde(citas.save(cita));
    }

    /** HU_014: citas del médico en una fecha, sin las canceladas. */
    public List<CitaResponse> agenda(LocalDate fecha, UsuarioActual actual) {
        Medico medico = medicoServicio.buscarMedico(actual.id());
        return citas.findByMedicoAndFechaHoraBetweenOrderByFechaHoraAsc(medico, RangoFechas.inicio(fecha), RangoFechas.fin(fecha))
                .stream()
                .filter(c -> c.getEstado() != EstadoCita.CANCELADA)
                .map(CitaResponse::desde)
                .toList();
    }

    /** HU_015: solo el médico de la cita (RN_12). */
    public CitaResponse registrarAtencion(String id, String notas, UsuarioActual actual) {
        Cita cita = citaDelMedico(id, actual);
        cita.registrarAtencion(notas, ahora());
        return CitaResponse.desde(citas.save(cita));
    }

    public CitaResponse marcarInasistencia(String id, UsuarioActual actual) {
        Cita cita = citaDelMedico(id, actual);
        cita.marcarInasistencia(ahora());
        return CitaResponse.desde(citas.save(cita));
    }

    private Paciente pacienteDeLaPeticion(String pacienteId, UsuarioActual actual) {
        if (actual.es(Rol.PACIENTE)) {
            return pacienteServicio.buscarPaciente(actual.id());
        }
        if (actual.es(Rol.RECEPCIONISTA)) {
            if (pacienteId == null || pacienteId.isBlank()) {
                throw new ReglaNegocioException("Indica el paciente para el que se agenda la cita (pacienteId)");
            }
            return pacienteServicio.buscarPaciente(pacienteId);
        }
        throw new AccesoDenegadoException("Solo los pacientes y las recepcionistas agendan citas");
    }

    /** RN_06: un paciente no puede tener dos citas a la misma hora. */
    private void verificarPacienteSinCruce(Paciente paciente, LocalDateTime fechaHora) {
        if (citas.existsByPacienteAndFechaHoraAndEstado(paciente, fechaHora, EstadoCita.PROGRAMADA)) {
            throw new ConflictoException("El paciente ya tiene otra cita a esa hora");
        }
    }

    private void verificarPuedeModificar(Cita cita, UsuarioActual actual) {
        boolean puede = actual.es(Rol.RECEPCIONISTA) || (actual.es(Rol.PACIENTE) && cita.esDelPaciente(actual.id()));
        if (!puede) {
            throw new AccesoDenegadoException("Solo el paciente de la cita o una recepcionista pueden modificarla");
        }
    }

    private Cita citaDelMedico(String id, UsuarioActual actual) {
        Cita cita = buscarCita(id);
        if (!cita.esDelMedico(actual.id())) {
            throw new AccesoDenegadoException("Solo el médico de la cita puede registrar la atención");
        }
        return cita;
    }

    private Cita buscarCita(String id) {
        return citas.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("La cita no existe"));
    }

    private LocalDateTime ahora() {
        return LocalDateTime.now(reloj);
    }
}
