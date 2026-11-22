package com.citasalud.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

import com.citasalud.model.HorarioAtencion;

import jakarta.validation.constraints.NotNull;

/** Un bloque de atención: día de la semana (MONDAY...SUNDAY) y horas en formato HH:mm. */
public record HorarioDto(
        @NotNull(message = "es obligatorio (MONDAY, TUESDAY, ...)") DayOfWeek diaSemana,
        @NotNull(message = "es obligatoria (HH:mm)") LocalTime horaInicio,
        @NotNull(message = "es obligatoria (HH:mm)") LocalTime horaFin) {

    public HorarioAtencion aModelo() {
        return new HorarioAtencion(diaSemana, horaInicio, horaFin);
    }

    public static HorarioDto desde(HorarioAtencion h) {
        return new HorarioDto(h.getDiaSemana(), h.getHoraInicio(), h.getHoraFin());
    }
}
