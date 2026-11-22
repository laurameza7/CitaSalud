package com.citasalud.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.citasalud.Fixtures;

/** RF_018: el reporte agrupa por especialidad y no suma las canceladas. */
class ReporteCitasTest {

    @Test
    @DisplayName("Agrupa por especialidad, cuenta estados y suma lo recaudado")
    void reporte() {
        var ahora = Fixtures.LUNES.minusDays(3).atTime(8, 0);
        Medico general = Fixtures.medico("m1", Fixtures.especialidad("General", 50000));
        Medico pediatra = Fixtures.medico("m2", Fixtures.especialidad("Pediatría", 80000));
        Paciente p = Fixtures.paciente("p1", TipoPaciente.PARTICULAR);

        Cita c1 = general.crearCita(p, Fixtures.LUNES.atTime(8, 0), "", ahora);   // 55.000
        Cita c2 = general.crearCita(p, Fixtures.LUNES.atTime(8, 30), "", ahora);  // 55.000, cancelada
        c2.cancelar(ahora);
        Cita c3 = pediatra.crearCita(p, Fixtures.LUNES.atTime(9, 0), "", ahora);  // 85.000

        ReporteCitas reporte = new ReporteCitas(Fixtures.LUNES, Fixtures.LUNES, List.of(c1, c2, c3));

        assertEquals(2, reporte.getLineas().size());
        LineaReporte lineaGeneral = reporte.getLineas().get(0);
        assertEquals("General", lineaGeneral.getEspecialidad());
        assertEquals(2, lineaGeneral.getTotal());
        assertEquals(1, lineaGeneral.getCanceladas());
        assertEquals(55000, lineaGeneral.getRecaudado());
        assertEquals(3, reporte.totalCitas());
        assertEquals(140000, reporte.totalRecaudado());
    }
}
