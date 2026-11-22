package com.citasalud.dto;

import jakarta.validation.constraints.NotBlank;

/** Datos para iniciar sesión (HU_002). */
public record LoginRequest(
        @NotBlank(message = "es obligatorio") String correo,
        @NotBlank(message = "es obligatoria") String contrasena) {
}
