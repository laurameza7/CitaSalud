package com.citasalud.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.citasalud.Fixtures;
import com.citasalud.exception.ReglaNegocioException;

/** RN_04: horario del médico y creación de citas. */
class MedicoTest {

    private final Especialidad especialidad = Fixtures.especialidad("Pediatría", 80000);
    private final Medico medico = Fixtures.medico("m1", especialidad);
    private final Paciente paciente = Fixtures.paciente("p1", TipoPaciente.PREPAGADA);

    @Test
    @DisplayName("De 08:00 a 10:00 hay 4 franjas de 30 minutos")
    void franjasDelDia() {
        assertEquals(4, medico.franjasDelDia(Fixtures.LUNES).size());
        assertEquals(Fixtures.LUNES.atTime(9, 30), medico.franjasDelDia(Fixtures.LUNES).get(3));
        assertTrue(medico.franjasDelDia(Fixtures.LUNES.plusDays(1)).isEmpty());
    }

    @Test
    @DisplayName("Solo atiende en franjas exactas dentro del horario")
    void atiendeEn() {
        assertTrue(medico.atiendeEn(Fixtures.LUNES.atTime(8, 30)));
        assertFalse(medico.atiendeEn(Fixtures.LUNES.atTime(8, 15)));
        assertFalse(medico.atiendeEn(Fixtures.LUNES.atTime(10, 0)));
    }

    @Test
    @DisplayName("Rechaza un horario que termina antes de empezar")
    void horarioInvalido() {
        var malo = new HorarioAtencion(DayOfWeek.FRIDAY, LocalTime.of(12, 0), LocalTime.of(8, 0));
        assertThrows(ReglaNegocioException.class, () -> medico.definirHorarios(List.of(malo)));
    }

    @Test
    @DisplayName("Crea la cita programada con el valor según el tipo de paciente")
    void crearCita() {
        Cita cita = medico.crearCita(paciente, Fixtures.LUNES.atTime(9, 0), "Control", Fixtures.LUNES.minusDays(5).atTime(8, 0));
        assertEquals(EstadoCita.PROGRAMADA, cita.getEstado());
        assertEquals(61000, cita.getValorPagado()); // 80.000 - 30 % + 5.000
    }

    @Test
    @DisplayName("No crea citas fuera del horario ni en el pasado")
    void crearCitaInvalida() {
        var ahora = Fixtures.LUNES.minusDays(1).atTime(8, 0);
        assertThrows(ReglaNegocioException.class, () -> medico.crearCita(paciente, Fixtures.LUNES.atTime(15, 0), "x", ahora));
        assertThrows(ReglaNegocioException.class,
                () -> medico.crearCita(paciente, Fixtures.LUNES.atTime(8, 0), "x", Fixtures.LUNES.atTime(9, 0)));
    }
}
