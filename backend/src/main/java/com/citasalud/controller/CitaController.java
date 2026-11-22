package com.citasalud.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.citasalud.dto.AgendarCitaRequest;
import com.citasalud.dto.AtencionRequest;
import com.citasalud.dto.CitaResponse;
import com.citasalud.dto.ReprogramarCitaRequest;
import com.citasalud.security.UsuarioActual;
import com.citasalud.service.CitaServicio;

import jakarta.validation.Valid;

/** GRASP Controlador de las citas (HU_009 a HU_015). No contiene reglas: todo lo delega en CitaServicio. */
@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaServicio citaServicio;

    public CitaController(CitaServicio citaServicio) {
        this.citaServicio = citaServicio;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('PACIENTE', 'RECEPCIONISTA')")
    public CitaResponse agendar(@Valid @RequestBody AgendarCitaRequest datos,
                                @AuthenticationPrincipal UsuarioActual actual) {
        return citaServicio.agendar(datos, actual);
    }

    @GetMapping("/mias")
    @PreAuthorize("hasRole('PACIENTE')")
    public List<CitaResponse> misCitas(@AuthenticationPrincipal UsuarioActual actual) {
        return citaServicio.misCitas(actual);
    }

    @GetMapping("/agenda")
    @PreAuthorize("hasRole('MEDICO')")
    public List<CitaResponse> agenda(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                     @AuthenticationPrincipal UsuarioActual actual) {
        return citaServicio.agenda(fecha, actual);
    }

    @GetMapping("/{id}")
    public CitaResponse obtener(@PathVariable String id, @AuthenticationPrincipal UsuarioActual actual) {
        return citaServicio.obtener(id, actual);
    }

    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('PACIENTE', 'RECEPCIONISTA')")
    public CitaResponse cancelar(@PathVariable String id, @AuthenticationPrincipal UsuarioActual actual) {
        return citaServicio.cancelar(id, actual);
    }

    @PutMapping("/{id}/reprogramar")
    @PreAuthorize("hasAnyRole('PACIENTE', 'RECEPCIONISTA')")
    public CitaResponse reprogramar(@PathVariable String id, @Valid @RequestBody ReprogramarCitaRequest datos,
                                    @AuthenticationPrincipal UsuarioActual actual) {
        return citaServicio.reprogramar(id, datos.nuevaFechaHora(), actual);
    }

    @PutMapping("/{id}/atencion")
    @PreAuthorize("hasRole('MEDICO')")
    public CitaResponse registrarAtencion(@PathVariable String id, @Valid @RequestBody AtencionRequest datos,
                                         @AuthenticationPrincipal UsuarioActual actual) {
        return citaServicio.registrarAtencion(id, datos.notas(), actual);
    }

    @PutMapping("/{id}/inasistencia")
    @PreAuthorize("hasRole('MEDICO')")
    public CitaResponse marcarInasistencia(@PathVariable String id, @AuthenticationPrincipal UsuarioActual actual) {
        return citaServicio.marcarInasistencia(id, actual);
    }
}
