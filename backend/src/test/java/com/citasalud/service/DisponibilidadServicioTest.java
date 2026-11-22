package com.citasalud.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.citasalud.Fixtures;
import com.citasalud.model.Cita;
import com.citasalud.model.EstadoCita;
import com.citasalud.model.Medico;
import com.citasalud.model.TipoPaciente;
import com.citasalud.repository.CitaRepositorio;

/** HU_008: franjas libres descontando ocupadas y pasadas. */
class DisponibilidadServicioTest {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    @Test
    @DisplayName("Quita la franja ocupada de las 08:30 y las que ya pasaron")
    void franjasLibres() {
        CitaRepositorio citas = mock(CitaRepositorio.class);
        Medico medico = Fixtures.medico("m1", Fixtures.especialidad("General", 50000));
        Cita ocupada = medico.crearCita(Fixtures.paciente("p1", TipoPaciente.PARTICULAR), Fixtures.LUNES.atTime(8, 30), "",
                Fixtures.LUNES.minusDays(2).atTime(8, 0));
        when(citas.findByMedicoAndEstadoAndFechaHoraBetween(eq(medico), eq(EstadoCita.PROGRAMADA), any(), any()))
                .thenReturn(List.of(ocupada));

        // Son las 08:05 del lunes: la franja de las 08:00 ya pasó.
        Clock reloj = Clock.fixed(Fixtures.LUNES.atTime(8, 5).atZone(BOGOTA).toInstant(), BOGOTA);
        DisponibilidadServicio servicio = new DisponibilidadServicio(mock(MedicoServicio.class), citas, reloj);

        List<LocalDateTime> libres = servicio.franjasLibres(medico, Fixtures.LUNES);

        assertEquals(List.of(Fixtures.LUNES.atTime(9, 0), Fixtures.LUNES.atTime(9, 30)), libres);
    }
}
