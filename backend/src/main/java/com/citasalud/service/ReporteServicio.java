package com.citasalud.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.citasalud.dto.ReporteResponse;
import com.citasalud.exception.ReglaNegocioException;
import com.citasalud.model.ReporteCitas;
import com.citasalud.repository.CitaRepositorio;

/** Reporte de citas por especialidad (HU_016). */
@Service
public class ReporteServicio {

    private final CitaRepositorio citas;

    public ReporteServicio(CitaRepositorio citas) {
        this.citas = citas;
    }

    public ReporteResponse generar(LocalDate desde, LocalDate hasta) {
        if (hasta.isBefore(desde)) {
            throw new ReglaNegocioException("La fecha final no puede ser anterior a la inicial");
        }
        var lista = citas.findByFechaHoraBetween(RangoFechas.inicio(desde), RangoFechas.fin(hasta));
        return ReporteResponse.desde(new ReporteCitas(desde, hasta, lista));
    }
}
