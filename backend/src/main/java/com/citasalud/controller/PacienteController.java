package com.citasalud.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.citasalud.dto.UsuarioResponse;
import com.citasalud.service.PacienteServicio;

/** Búsqueda de pacientes por documento (HU_013). */
@RestController
@RequestMapping("/api/pacientes")
@PreAuthorize("hasAnyRole('RECEPCIONISTA', 'ADMINISTRADOR')")
public class PacienteController {

    private final PacienteServicio pacienteServicio;

    public PacienteController(PacienteServicio pacienteServicio) {
        this.pacienteServicio = pacienteServicio;
    }

    @GetMapping
    public UsuarioResponse buscar(@RequestParam String documento) {
        return pacienteServicio.buscarPorDocumento(documento);
    }
}
