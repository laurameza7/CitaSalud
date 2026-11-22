package com.citasalud.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.citasalud.dto.LoginRequest;
import com.citasalud.dto.LoginResponse;
import com.citasalud.dto.RegistroPacienteRequest;
import com.citasalud.dto.UsuarioResponse;
import com.citasalud.security.UsuarioActual;
import com.citasalud.service.AuthServicio;

import jakarta.validation.Valid;

/** GRASP Controlador: recibe las peticiones de cuenta y delega en AuthServicio (HU_001, HU_002). */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthServicio authServicio;

    public AuthController(AuthServicio authServicio) {
        this.authServicio = authServicio;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@Valid @RequestBody RegistroPacienteRequest datos) {
        return authServicio.registrar(datos);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest datos) {
        return authServicio.login(datos);
    }

    @GetMapping("/yo")
    public UsuarioResponse yo(@AuthenticationPrincipal UsuarioActual actual) {
        return authServicio.yo(actual.id());
    }
}
