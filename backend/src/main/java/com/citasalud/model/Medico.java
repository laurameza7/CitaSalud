package com.citasalud.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.citasalud.exception.ReglaNegocioException;

/** Médico de la clínica. Tiene una especialidad y un horario de atención semanal. */
@Document(collection = "usuarios")
public class Medico extends Usuario {

    private String registroMedico;
    @DBRef
    private Especialidad especialidad;
    private String consultorio;
    private List<HorarioAtencion> horarios = new ArrayList<>();

    protected Medico() {
    }

    public Medico(String nombre, String correo, String contrasenaHash, String telefono,
                  String registroMedico, Especialidad especialidad, String consultorio) {
        super(nombre, correo, contrasenaHash, telefono, Rol.MEDICO);
        this.registroMedico = registroMedico;
        this.especialidad = especialidad;
        this.consultorio = consultorio;
    }

    /** GRASP Experto: el médico conoce su horario y sabe si atiende en una fecha y hora (RN_04). */
    public boolean atiendeEn(LocalDateTime inicio) {
        return horarios.stream().anyMatch(h -> h.contiene(inicio));
    }

    /** Todas las franjas de atención de una fecha, ordenadas, sin descontar las ocupadas. */
    public List<LocalDateTime> franjasDelDia(LocalDate fecha) {
        return horarios.stream()
                .filter(h -> h.getDiaSemana() == fecha.getDayOfWeek())
                .flatMap(h -> h.franjas().stream())
                .map(fecha::atTime)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    /** Reemplaza el horario validando cada bloque (RF_006). */
    public void definirHorarios(List<HorarioAtencion> nuevos) {
        for (HorarioAtencion h : nuevos) {
            if (!h.esValido()) {
                throw new ReglaNegocioException(
                        "El horario del " + h.getDiaSemana() + " no es válido: la hora de fin debe ser posterior a la de inicio "
                                + "y debe caber al menos una cita de " + Cita.DURACION_MINUTOS + " minutos");
            }
        }
        this.horarios = new ArrayList<>(nuevos);
    }

    /**
     * GRASP Creador: el médico tiene la agenda, así que crea las citas que caben en ella.
     * El valor se calcula con la tarifa de su especialidad y el tipo del paciente.
     */
    public Cita crearCita(Paciente paciente, LocalDateTime fechaHora, String motivo, LocalDateTime ahora) {
        if (!fechaHora.isAfter(ahora)) {
            throw new ReglaNegocioException("La cita debe ser en una fecha y hora futura");
        }
        if (!atiendeEn(fechaHora)) {
            throw new ReglaNegocioException("El médico no atiende en esa fecha y hora");
        }
        long valor = especialidad.valorPara(paciente.getTipoPaciente());
        return new Cita(paciente, this, especialidad, fechaHora, motivo, valor, ahora);
    }

    public String getRegistroMedico() { return registroMedico; }
    public void setRegistroMedico(String registroMedico) { this.registroMedico = registroMedico; }
    public Especialidad getEspecialidad() { return especialidad; }
    public void setEspecialidad(Especialidad especialidad) { this.especialidad = especialidad; }
    public String getConsultorio() { return consultorio; }
    public void setConsultorio(String consultorio) { this.consultorio = consultorio; }
    public List<HorarioAtencion> getHorarios() { return horarios; }
    public void setHorarios(List<HorarioAtencion> horarios) { this.horarios = horarios; }
}
