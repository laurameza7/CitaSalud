package com.citasalud.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

/** Nueva fecha y hora para una cita (HU_012). */
public record ReprogramarCitaRequest(@NotNull(message = "es obligatoria (AAAA-MM-DDTHH:mm)") LocalDateTime nuevaFechaHora) {
}
