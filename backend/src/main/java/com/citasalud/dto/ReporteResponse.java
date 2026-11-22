package com.citasalud.dto;

import java.time.LocalDate;
import java.util.List;

import com.citasalud.model.LineaReporte;
import com.citasalud.model.ReporteCitas;

/** Reporte de citas por especialidad (RF_018). */
public record ReporteResponse(LocalDate desde, LocalDate hasta, List<Linea> lineas, int totalCitas, long totalRecaudado) {

    public record Linea(String especialidad, int programadas, int atendidas, int canceladas, int noAsistio,
                        int total, long recaudado) {

        static Linea desde(LineaReporte l) {
            return new Linea(l.getEspecialidad(), l.getProgramadas(), l.getAtendidas(), l.getCanceladas(),
                    l.getNoAsistio(), l.getTotal(), l.getRecaudado());
        }
    }

    public static ReporteResponse desde(ReporteCitas r) {
        return new ReporteResponse(r.getDesde(), r.getHasta(), r.getLineas().stream().map(Linea::desde).toList(),
                r.totalCitas(), r.totalRecaudado());
    }
}
