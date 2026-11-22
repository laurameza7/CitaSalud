package com.citasalud.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Franjas libres de un médico en una fecha (RF_008). */
public record DisponibilidadResponse(String medicoId, String medico, LocalDate fecha, List<LocalTime> franjasLibres) {
}
