package com.citasalud.dto;

import com.citasalud.model.Especialidad;

public record EspecialidadResponse(String id, String nombre, String descripcion, long tarifa, boolean activa) {

    public static EspecialidadResponse desde(Especialidad e) {
        return new EspecialidadResponse(e.getId(), e.getNombre(), e.getDescripcion(), e.getTarifa(), e.isActiva());
    }
}
