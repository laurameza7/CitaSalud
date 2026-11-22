package com.citasalud.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Datos para registrar o editar un médico (HU_004).
 * Al editar, la contraseña se puede dejar vacía para no cambiarla.
 */
public record MedicoRequest(
        @NotBlank(message = "es obligatorio") String nombre,
        @NotBlank(message = "es obligatorio") @Email(message = "no es un correo válido") String correo,
        String contrasena,
        @NotBlank(message = "es obligatorio") String telefono,
        @NotBlank(message = "es obligatorio") String registroMedico,
        @NotBlank(message = "es obligatoria") String especialidadId,
        @NotBlank(message = "es obligatorio") String consultorio) {
}
