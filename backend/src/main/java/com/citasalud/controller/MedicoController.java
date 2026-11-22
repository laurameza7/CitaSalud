package com.citasalud.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.citasalud.dto.DisponibilidadResponse;
import com.citasalud.dto.HorarioDto;
import com.citasalud.dto.MedicoRequest;
import com.citasalud.dto.MedicoResponse;
import com.citasalud.security.UsuarioActual;
import com.citasalud.service.DisponibilidadServicio;
import com.citasalud.service.MedicoServicio;

import jakarta.validation.Valid;

/** Médicos, su horario y su disponibilidad (HU_004, HU_006, HU_007, HU_008). */
@RestController
@RequestMapping("/api/medicos")
public class MedicoController {

    private final MedicoServicio medicoServicio;
    private final DisponibilidadServicio disponibilidadServicio;

    public MedicoController(MedicoServicio medicoServicio, DisponibilidadServicio disponibilidadServicio) {
        this.medicoServicio = medicoServicio;
        this.disponibilidadServicio = disponibilidadServicio;
    }

    @GetMapping
    public List<MedicoResponse> listar(@RequestParam(required = false) String especialidadId,
                                       @RequestParam(defaultValue = "false") boolean incluirInactivos) {
        return medicoServicio.listar(especialidadId, incluirInactivos);
    }

    @GetMapping("/{id}")
    public MedicoResponse obtener(@PathVariable String id) {
        return medicoServicio.obtener(id);
    }

    @GetMapping("/{id}/disponibilidad")
    public DisponibilidadResponse disponibilidad(@PathVariable String id,
                                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return disponibilidadServicio.consultar(id, fecha);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public MedicoResponse crear(@Valid @RequestBody MedicoRequest datos) {
        return medicoServicio.crear(datos);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public MedicoResponse actualizar(@PathVariable String id, @Valid @RequestBody MedicoRequest datos) {
        return medicoServicio.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void desactivar(@PathVariable String id) {
        medicoServicio.desactivar(id);
    }

    @PutMapping("/{id}/horarios")
    @PreAuthorize("hasAnyRole('MEDICO', 'ADMINISTRADOR')")
    public MedicoResponse actualizarHorarios(@PathVariable String id, @RequestBody List<@Valid HorarioDto> horarios,
                                             @AuthenticationPrincipal UsuarioActual actual) {
        return medicoServicio.actualizarHorarios(id, horarios, actual);
    }
}
