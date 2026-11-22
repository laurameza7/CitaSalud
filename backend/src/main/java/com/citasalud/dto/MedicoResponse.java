package com.citasalud.dto;

import java.util.List;

import com.citasalud.model.Medico;

public record MedicoResponse(String id, String nombre, String correo, String telefono, String registroMedico,
                             String especialidadId, String especialidad, String consultorio, boolean activo,
                             List<HorarioDto> horarios) {

    public static MedicoResponse desde(Medico m) {
        return new MedicoResponse(m.getId(), m.getNombre(), m.getCorreo(), m.getTelefono(), m.getRegistroMedico(),
                m.getEspecialidad().getId(), m.getEspecialidad().getNombre(), m.getConsultorio(), m.isActivo(),
                m.getHorarios().stream().map(HorarioDto::desde).toList());
    }
}
