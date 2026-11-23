package com.citasalud.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.citasalud.dto.RecordatorioResponse;
import com.citasalud.security.UsuarioActual;
import com.citasalud.service.RecordatorioServicio;

/** GRASP Controlador de los recordatorios (HU_017). */
@RestController
@RequestMapping("/api/recordatorios")
public class RecordatorioController {

    private final RecordatorioServicio servicio;

    public RecordatorioController(RecordatorioServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/mios")
    @PreAuthorize("hasRole('PACIENTE')")
    public List<RecordatorioResponse> mios(@AuthenticationPrincipal UsuarioActual actual) {
        return servicio.misRecordatorios(actual);
    }

    @PutMapping("/{id}/leido")
    @PreAuthorize("hasRole('PACIENTE')")
    public RecordatorioResponse marcarLeido(@PathVariable String id, @AuthenticationPrincipal UsuarioActual actual) {
        return servicio.marcarLeido(id, actual);
    }

    /** Ejecuta la revisión en el momento, sin esperar los 15 minutos (útil para la demostración). */
    @PostMapping("/generar")
    @PreAuthorize("hasAnyRole('RECEPCIONISTA', 'ADMINISTRADOR')")
    public Map<String, Integer> generar() {
        return Map.of("recordatoriosCreados", servicio.generarRecordatorios());
    }
}
