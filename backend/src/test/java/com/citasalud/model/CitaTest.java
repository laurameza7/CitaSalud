package com.citasalud.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.citasalud.Fixtures;
import com.citasalud.exception.ReglaNegocioException;

/** RN_07, RN_08, RN_11 y RN_12. La cita es el lunes a las 08:00. */
class CitaTest {

    private Cita cita;
    private final LocalDateTime tresDiasAntes = Fixtures.LUNES.minusDays(3).atTime(8, 0);

    @BeforeEach
    void preparar() {
        Medico medico = Fixtures.medico("m1", Fixtures.especialidad("General", 50000));
        cita = medico.crearCita(Fixtures.paciente("p1", TipoPaciente.PARTICULAR), Fixtures.LUNES.atTime(8, 0), "Control",
                tresDiasAntes);
    }

    @Test
    @DisplayName("Se cancela si faltan más de 24 horas")
    void cancelarATiempo() {
        cita.cancelar(tresDiasAntes);
        assertEquals(EstadoCita.CANCELADA, cita.getEstado());
        assertEquals(0, cita.valorRecaudado());
    }

    @Test
    @DisplayName("No se cancela si faltan 24 horas o menos")
    void cancelarTarde() {
        LocalDateTime cincoHorasAntes = Fixtures.LUNES.atTime(3, 0);
        assertThrows(ReglaNegocioException.class, () -> cita.cancelar(cincoHorasAntes));
        assertEquals(EstadoCita.PROGRAMADA, cita.getEstado());
    }

    @Test
    @DisplayName("Se reprograma máximo 2 veces")
    void reprogramarMaximoDos() {
        cita.reprogramar(Fixtures.LUNES.atTime(8, 30), tresDiasAntes);
        cita.reprogramar(Fixtures.LUNES.atTime(9, 0), tresDiasAntes);
        assertEquals(2, cita.getReprogramaciones());
        assertThrows(ReglaNegocioException.class, () -> cita.reprogramar(Fixtures.LUNES.atTime(9, 30), tresDiasAntes));
    }

    @Test
    @DisplayName("No se reprograma fuera del horario del médico")
    void reprogramarFueraDeHorario() {
        assertThrows(ReglaNegocioException.class, () -> cita.reprogramar(Fixtures.LUNES.atTime(18, 0), tresDiasAntes));
    }

    @Test
    @DisplayName("La atención solo se registra cuando la cita ya empezó")
    void registrarAtencion() {
        assertThrows(ReglaNegocioException.class, () -> cita.registrarAtencion("notas", tresDiasAntes));
        cita.registrarAtencion("Paciente estable", Fixtures.LUNES.atTime(8, 10));
        assertEquals(EstadoCita.ATENDIDA, cita.getEstado());
        assertEquals("Paciente estable", cita.getNotasAtencion());
    }

    @Test
    @DisplayName("Se marca inasistencia y ya no se puede cancelar")
    void inasistencia() {
        cita.marcarInasistencia(Fixtures.LUNES.atTime(8, 40));
        assertEquals(EstadoCita.NO_ASISTIO, cita.getEstado());
        assertThrows(ReglaNegocioException.class, () -> cita.cancelar(tresDiasAntes));
    }
}
