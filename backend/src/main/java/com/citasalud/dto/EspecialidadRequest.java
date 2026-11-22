package com.citasalud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/** Datos para crear o editar una especialidad (HU_003). */
public record EspecialidadRequest(
        @NotBlank(message = "es obligatorio") String nombre,
        String descripcion,
        @Positive(message = "debe ser mayor que cero") long tarifa) {
}
