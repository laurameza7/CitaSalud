package com.citasalud.dto;

import java.time.LocalDate;

import com.citasalud.model.TipoPaciente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

/** Datos para registrarse como paciente (HU_001). */
public record RegistroPacienteRequest(
        @NotBlank(message = "es obligatorio") String nombre,
        @NotBlank(message = "es obligatorio") String documento,
        @NotBlank(message = "es obligatorio") @Email(message = "no es un correo válido") String correo,
        @NotBlank(message = "es obligatorio") String telefono,
        @NotNull(message = "es obligatoria") @Past(message = "debe ser una fecha pasada") LocalDate fechaNacimiento,
        @NotNull(message = "es obligatorio (PARTICULAR, CONVENIO o PREPAGADA)") TipoPaciente tipoPaciente,
        @NotBlank(message = "es obligatoria") @Size(min = 8, message = "debe tener al menos 8 caracteres") String contrasena) {
}
