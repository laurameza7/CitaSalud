package com.citasalud.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Ayuda para consultar "todo un día" en MongoDB.
 * Las consultas ...Between de Spring Data excluyen los extremos, por eso el inicio
 * se corre un segundo hacia atrás: así la cita de las 00:00 también entra.
 */
final class RangoFechas {

    private RangoFechas() {
    }

    static LocalDateTime inicio(LocalDate fecha) {
        return fecha.atStartOfDay().minusSeconds(1);
    }

    static LocalDateTime fin(LocalDate fecha) {
        return fecha.plusDays(1).atStartOfDay();
    }
}
