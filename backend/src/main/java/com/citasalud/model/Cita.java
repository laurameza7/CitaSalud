package com.citasalud.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.citasalud.exception.ReglaNegocioException;

/**
 * Cita médica entre un paciente y un médico. Guarda sus propias reglas de cambio de estado
 * (GRASP Experto): cuándo se puede cancelar, reprogramar o atender.
 */
@Document(collection = "citas")
public class Cita {

    /** Duración de cada cita en minutos (RN_04). */
    public static final int DURACION_MINUTOS = 30;
    /** Horas mínimas de anticipación para cancelar o reprogramar (RN_08). */
    public static final int HORAS_MINIMAS_PARA_CAMBIOS = 24;
    /** Máximo de veces que se puede reprogramar una cita (RN_11). */
    public static final int MAXIMO_REPROGRAMACIONES = 2;

    @Id
    private String id;
    @DBRef
    private Paciente paciente;
    @DBRef
    private Medico medico;
    @DBRef
    private Especialidad especialidad;
    @Indexed
    private LocalDateTime fechaHora;
    private EstadoCita estado;
    private String motivo;
    private long valorPagado;
    private String referenciaPago;
    private boolean reembolsada;
    private int reprogramaciones;
    private String notasAtencion;
    private LocalDateTime creadaEn;

    protected Cita() {
    }

    public Cita(Paciente paciente, Medico medico, Especialidad especialidad, LocalDateTime fechaHora,
                String motivo, long valorPagado, LocalDateTime creadaEn) {
        this.paciente = paciente;
        this.medico = medico;
        this.especialidad = especialidad;
        this.fechaHora = fechaHora;
        this.motivo = motivo;
        this.valorPagado = valorPagado;
        this.creadaEn = creadaEn;
        this.estado = EstadoCita.PROGRAMADA;
    }

    public boolean estaProgramada() {
        return estado == EstadoCita.PROGRAMADA;
    }

    /** RN_08: faltan más de 24 horas para la cita. */
    public boolean faltanMasDeHorasMinimas(LocalDateTime ahora) {
        return ahora.plusHours(HORAS_MINIMAS_PARA_CAMBIOS).isBefore(fechaHora);
    }

    public boolean puedeCancelarse(LocalDateTime ahora) {
        return estaProgramada() && faltanMasDeHorasMinimas(ahora);
    }

    public boolean puedeReprogramarse(LocalDateTime ahora) {
        return puedeCancelarse(ahora) && reprogramaciones < MAXIMO_REPROGRAMACIONES;
    }

    public void cancelar(LocalDateTime ahora) {
        exigirProgramada();
        if (!faltanMasDeHorasMinimas(ahora)) {
            throw new ReglaNegocioException("La cita solo se puede cancelar con más de "
                    + HORAS_MINIMAS_PARA_CAMBIOS + " horas de anticipación");
        }
        this.estado = EstadoCita.CANCELADA;
    }

    public void reprogramar(LocalDateTime nuevaFechaHora, LocalDateTime ahora) {
        exigirProgramada();
        if (!faltanMasDeHorasMinimas(ahora)) {
            throw new ReglaNegocioException("La cita solo se puede reprogramar con más de "
                    + HORAS_MINIMAS_PARA_CAMBIOS + " horas de anticipación");
        }
        if (reprogramaciones >= MAXIMO_REPROGRAMACIONES) {
            throw new ReglaNegocioException("La cita ya se reprogramó el máximo de "
                    + MAXIMO_REPROGRAMACIONES + " veces");
        }
        if (!nuevaFechaHora.isAfter(ahora)) {
            throw new ReglaNegocioException("La nueva fecha debe ser futura");
        }
        if (!medico.atiendeEn(nuevaFechaHora)) {
            throw new ReglaNegocioException("El médico no atiende en la nueva fecha y hora");
        }
        this.fechaHora = nuevaFechaHora;
        this.reprogramaciones++;
    }

    /** RN_12: solo cuando la hora de la cita ya llegó. */
    public void registrarAtencion(String notas, LocalDateTime ahora) {
        exigirProgramadaYEnCurso(ahora);
        this.notasAtencion = notas;
        this.estado = EstadoCita.ATENDIDA;
    }

    public void marcarInasistencia(LocalDateTime ahora) {
        exigirProgramadaYEnCurso(ahora);
        this.estado = EstadoCita.NO_ASISTIO;
    }

    public void registrarPago(String referencia) {
        this.referenciaPago = referencia;
    }

    public void marcarReembolsada() {
        this.reembolsada = true;
    }

    public boolean esDelPaciente(String pacienteId) {
        return paciente != null && paciente.getId() != null && paciente.getId().equals(pacienteId);
    }

    public boolean esDelMedico(String medicoId) {
        return medico != null && medico.getId() != null && medico.getId().equals(medicoId);
    }

    /** Lo que se recauda por esta cita: las canceladas se reembolsan, así que no cuentan. */
    public long valorRecaudado() {
        return estado == EstadoCita.CANCELADA ? 0 : valorPagado;
    }

    private void exigirProgramada() {
        if (!estaProgramada()) {
            throw new ReglaNegocioException("La cita no está programada (estado actual: " + estado + ")");
        }
    }

    private void exigirProgramadaYEnCurso(LocalDateTime ahora) {
        exigirProgramada();
        if (ahora.isBefore(fechaHora)) {
            throw new ReglaNegocioException("La cita todavía no ha empezado");
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
    public Medico getMedico() { return medico; }
    public void setMedico(Medico medico) { this.medico = medico; }
    public Especialidad getEspecialidad() { return especialidad; }
    public void setEspecialidad(Especialidad especialidad) { this.especialidad = especialidad; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public EstadoCita getEstado() { return estado; }
    public void setEstado(EstadoCita estado) { this.estado = estado; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public long getValorPagado() { return valorPagado; }
    public void setValorPagado(long valorPagado) { this.valorPagado = valorPagado; }
    public String getReferenciaPago() { return referenciaPago; }
    public void setReferenciaPago(String referenciaPago) { this.referenciaPago = referenciaPago; }
    public boolean isReembolsada() { return reembolsada; }
    public void setReembolsada(boolean reembolsada) { this.reembolsada = reembolsada; }
    public int getReprogramaciones() { return reprogramaciones; }
    public void setReprogramaciones(int reprogramaciones) { this.reprogramaciones = reprogramaciones; }
    public String getNotasAtencion() { return notasAtencion; }
    public void setNotasAtencion(String notasAtencion) { this.notasAtencion = notasAtencion; }
    public LocalDateTime getCreadaEn() { return creadaEn; }
    public void setCreadaEn(LocalDateTime creadaEn) { this.creadaEn = creadaEn; }
}
