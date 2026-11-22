package com.citasalud.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reporte de citas por especialidad en un rango de fechas (RF_018).
 * GRASP Creador: el reporte crea sus propias líneas porque las contiene y las agrega.
 * No se guarda en MongoDB: se calcula cada vez.
 */
public class ReporteCitas {

    private final LocalDate desde;
    private final LocalDate hasta;
    private final List<LineaReporte> lineas;

    public ReporteCitas(LocalDate desde, LocalDate hasta, List<Cita> citas) {
        this.desde = desde;
        this.hasta = hasta;
        Map<String, LineaReporte> porEspecialidad = new LinkedHashMap<>();
        for (Cita cita : citas) {
            String nombre = cita.getEspecialidad().getNombre();
            porEspecialidad.computeIfAbsent(nombre, LineaReporte::new).registrar(cita);
        }
        this.lineas = new ArrayList<>(porEspecialidad.values());
        this.lineas.sort(Comparator.comparing(LineaReporte::getEspecialidad));
    }

    public int totalCitas() {
        return lineas.stream().mapToInt(LineaReporte::getTotal).sum();
    }

    public long totalRecaudado() {
        return lineas.stream().mapToLong(LineaReporte::getRecaudado).sum();
    }

    public LocalDate getDesde() { return desde; }
    public LocalDate getHasta() { return hasta; }
    public List<LineaReporte> getLineas() { return lineas; }
}
