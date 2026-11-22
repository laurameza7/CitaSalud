package com.citasalud.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Datos para crear una recepcionista (HU_005). */
public record RecepcionistaRequest(
        @NotBlank(message = "es obligatorio") String nombre,
        @NotBlank(message = "es obligatorio") @Email(message = "no es un correo válido") String correo,
        @NotBlank(message = "es obligatoria") @Size(min = 8, message = "debe tener al menos 8 caracteres") String contrasena,
        @NotBlank(message = "es obligatorio") String telefono) {
}
