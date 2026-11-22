package com.citasalud.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.citasalud.dto.RecepcionistaRequest;
import com.citasalud.dto.UsuarioResponse;
import com.citasalud.service.RecepcionistaServicio;

import jakarta.validation.Valid;

/** Cuentas de recepcionista (HU_005). Solo el administrador. */
@RestController
@RequestMapping("/api/recepcionistas")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class RecepcionistaController {

    private final RecepcionistaServicio recepcionistaServicio;

    public RecepcionistaController(RecepcionistaServicio recepcionistaServicio) {
        this.recepcionistaServicio = recepcionistaServicio;
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return recepcionistaServicio.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(@Valid @RequestBody RecepcionistaRequest datos) {
        return recepcionistaServicio.crear(datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable String id) {
        recepcionistaServicio.desactivar(id);
    }
}
