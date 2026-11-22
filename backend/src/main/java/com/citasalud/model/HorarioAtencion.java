package com.citasalud.model;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Bloque de atención de un médico en un día de la semana, por ejemplo lunes de 08:00 a 12:00.
 * Se guarda dentro del documento del médico (no tiene colección propia).
 */
public class HorarioAtencion {

    private DayOfWeek diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;

    protected HorarioAtencion() {
    }

    public HorarioAtencion(DayOfWeek diaSemana, LocalTime horaInicio, LocalTime horaFin) {
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    /** El bloque es válido si termina después de empezar y cabe al menos una cita. */
    public boolean esValido() {
        return diaSemana != null && horaInicio != null && horaFin != null
                && minutosDeAtencion() >= Cita.DURACION_MINUTOS;
    }

    /** Horas de inicio de las franjas de este bloque (cada Cita.DURACION_MINUTOS minutos). */
    public List<LocalTime> franjas() {
        List<LocalTime> franjas = new ArrayList<>();
        long cantidad = minutosDeAtencion() / Cita.DURACION_MINUTOS;
        for (int i = 0; i < cantidad; i++) {
            franjas.add(horaInicio.plusMinutes((long) i * Cita.DURACION_MINUTOS));
        }
        return franjas;
    }

    private long minutosDeAtencion() {
        return Duration.between(horaInicio, horaFin).toMinutes();
    }

    /** Indica si una cita que empieza en esa fecha y hora cabe en este bloque. */
    public boolean contiene(LocalDateTime inicio) {
        return inicio.getDayOfWeek() == diaSemana && franjas().contains(inicio.toLocalTime());
    }

    public DayOfWeek getDiaSemana() { return diaSemana; }
    public void setDiaSemana(DayOfWeek diaSemana) { this.diaSemana = diaSemana; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
}
