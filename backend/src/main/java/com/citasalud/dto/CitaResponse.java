package com.citasalud.dto;

import java.time.LocalDateTime;

import com.citasalud.model.Cita;
import com.citasalud.model.EstadoCita;

public record CitaResponse(String id, String pacienteId, String paciente, String documentoPaciente,
                           String medicoId, String medico, String especialidad, String consultorio,
                           LocalDateTime fechaHora, EstadoCita estado, String motivo, long valorPagado,
                           String referenciaPago, boolean reembolsada, int reprogramaciones, String notasAtencion) {

    public static CitaResponse desde(Cita c) {
        return new CitaResponse(c.getId(), c.getPaciente().getId(), c.getPaciente().getNombre(),
                c.getPaciente().getDocumento(), c.getMedico().getId(), c.getMedico().getNombre(),
                c.getEspecialidad().getNombre(), c.getMedico().getConsultorio(), c.getFechaHora(), c.getEstado(),
                c.getMotivo(), c.getValorPagado(), c.getReferenciaPago(), c.isReembolsada(),
                c.getReprogramaciones(), c.getNotasAtencion());
    }
}
