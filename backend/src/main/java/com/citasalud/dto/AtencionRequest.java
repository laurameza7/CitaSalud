package com.citasalud.dto;

import jakarta.validation.constraints.NotBlank;

/** Notas que deja el médico al atender (HU_015). */
public record AtencionRequest(@NotBlank(message = "son obligatorias") String notas) {
}
