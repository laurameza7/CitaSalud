package com.citasalud.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Datos para agendar una cita (HU_009 y HU_013).
 * pacienteId solo lo envía la recepcionista; el paciente agenda para sí mismo.
 * fechaHora va en formato AAAA-MM-DDTHH:mm, por ejemplo 2026-12-01T09:00.
 */
public record AgendarCitaRequest(
        @NotBlank(message = "es obligatorio") String medicoId,
        @NotNull(message = "es obligatoria (AAAA-MM-DDTHH:mm)") LocalDateTime fechaHora,
        String motivo,
        @NotBlank(message = "es obligatorio") String numeroTarjeta,
        String pacienteId) {
}
