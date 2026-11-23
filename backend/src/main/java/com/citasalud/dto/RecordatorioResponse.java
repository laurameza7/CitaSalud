package com.citasalud.dto;

import java.time.LocalDateTime;

import com.citasalud.model.Recordatorio;

public record RecordatorioResponse(String id, String citaId, LocalDateTime fechaCita, String mensaje,
                                   LocalDateTime creadoEn, boolean leido) {

    public static RecordatorioResponse desde(Recordatorio r) {
        return new RecordatorioResponse(r.getId(), r.getCita().getId(), r.getFechaCita(), r.getMensaje(),
                r.getCreadoEn(), r.isLeido());
    }
}
