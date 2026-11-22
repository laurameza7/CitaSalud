package com.citasalud.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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

import com.citasalud.dto.EspecialidadRequest;
import com.citasalud.dto.EspecialidadResponse;
import com.citasalud.dto.ValorCitaResponse;
import com.citasalud.model.TipoPaciente;
import com.citasalud.service.EspecialidadServicio;

import jakarta.validation.Valid;

/** Especialidades (HU_003, HU_007) y valor de la cita (RF_010). */
@RestController
@RequestMapping("/api/especialidades")
public class EspecialidadController {

    private final EspecialidadServicio especialidadServicio;

    public EspecialidadController(EspecialidadServicio especialidadServicio) {
        this.especialidadServicio = especialidadServicio;
    }

    @GetMapping
    public List<EspecialidadResponse> listar(@RequestParam(defaultValue = "false") boolean incluirInactivas) {
        return especialidadServicio.listar(incluirInactivas);
    }

    @GetMapping("/{id}")
    public EspecialidadResponse obtener(@PathVariable String id) {
        return especialidadServicio.obtener(id);
    }

    @GetMapping("/{id}/valor")
    public ValorCitaResponse valor(@PathVariable String id, @RequestParam TipoPaciente tipoPaciente) {
        return especialidadServicio.valorPara(id, tipoPaciente);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public EspecialidadResponse crear(@Valid @RequestBody EspecialidadRequest datos) {
        return especialidadServicio.crear(datos);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public EspecialidadResponse actualizar(@PathVariable String id, @Valid @RequestBody EspecialidadRequest datos) {
        return especialidadServicio.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void desactivar(@PathVariable String id) {
        especialidadServicio.desactivar(id);
    }
}
