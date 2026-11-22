package com.citasalud.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.citasalud.dto.ReporteResponse;
import com.citasalud.service.ReporteServicio;

/** Reporte de citas por especialidad (HU_016). */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteServicio reporteServicio;

    public ReporteController(ReporteServicio reporteServicio) {
        this.reporteServicio = reporteServicio;
    }

    @GetMapping("/citas")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ReporteResponse citas(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteServicio.generar(desde, hasta);
    }
}
