package com.citasalud;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.citasalud.model.Especialidad;
import com.citasalud.model.HorarioAtencion;
import com.citasalud.model.Medico;
import com.citasalud.model.Paciente;
import com.citasalud.model.TipoPaciente;

/** Objetos de prueba reutilizables. El lunes 30 de noviembre de 2026 es el día de las citas de ejemplo. */
public final class Fixtures {

    public static final LocalDate LUNES = LocalDate.of(2026, 11, 30);

    private Fixtures() {
    }

    public static Especialidad especialidad(String nombre, long tarifa) {
        Especialidad e = new Especialidad(nombre, "Descripción", tarifa);
        e.setId("esp-" + nombre);
        return e;
    }

    /** Médico que atiende los lunes de 08:00 a 10:00 (4 franjas). */
    public static Medico medico(String id, Especialidad especialidad) {
        Medico m = new Medico("Dr. " + id, id + "@citasalud.com", "hash", "300", "RM-1", especialidad, "101");
        m.setId(id);
        m.definirHorarios(List.of(new HorarioAtencion(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(10, 0))));
        return m;
    }

    public static Paciente paciente(String id, TipoPaciente tipo) {
        Paciente p = new Paciente("Paciente " + id, id + "@correo.com", "hash", "310", "doc-" + id,
                LocalDate.of(1990, 1, 1), tipo);
        p.setId(id);
        return p;
    }
}
