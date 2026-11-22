package com.citasalud.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.citasalud.dto.DisponibilidadResponse;
import com.citasalud.exception.ConflictoException;
import com.citasalud.model.Cita;
import com.citasalud.model.EstadoCita;
import com.citasalud.model.Medico;
import com.citasalud.repository.CitaRepositorio;

/** Calcula las franjas libres de un médico (HU_008, RN_04, RN_05). */
@Service
public class DisponibilidadServicio {

    private final MedicoServicio medicoServicio;
    private final CitaRepositorio citas;
    private final Clock reloj;

    public DisponibilidadServicio(MedicoServicio medicoServicio, CitaRepositorio citas, Clock reloj) {
        this.medicoServicio = medicoServicio;
        this.citas = citas;
        this.reloj = reloj;
    }

    public DisponibilidadResponse consultar(String medicoId, LocalDate fecha) {
        Medico medico = medicoServicio.buscarMedico(medicoId);
        List<LocalTime> horas = franjasLibres(medico, fecha).stream().map(LocalDateTime::toLocalTime).toList();
        return new DisponibilidadResponse(medico.getId(), medico.getNombre(), fecha, horas);
    }

    /** Franjas del horario del médico que no han pasado y no tienen una cita programada. */
    public List<LocalDateTime> franjasLibres(Medico medico, LocalDate fecha) {
        if (!medico.isActivo()) {
            return List.of();
        }
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Set<LocalDateTime> ocupadas = citas.findByMedicoAndEstadoAndFechaHoraBetween(medico, EstadoCita.PROGRAMADA,
                        RangoFechas.inicio(fecha), RangoFechas.fin(fecha)).stream()
                .map(Cita::getFechaHora)
                .collect(Collectors.toSet());
        return medico.franjasDelDia(fecha).stream()
                .filter(franja -> franja.isAfter(ahora))
                .filter(franja -> !ocupadas.contains(franja))
                .toList();
    }

    /** RN_05: un médico no puede tener dos citas programadas en la misma franja. */
    public void verificarFranjaLibre(Medico medico, LocalDateTime fechaHora) {
        if (citas.existsByMedicoAndFechaHoraAndEstado(medico, fechaHora, EstadoCita.PROGRAMADA)) {
            throw new ConflictoException("La franja de las " + fechaHora.toLocalTime() + " ya no está disponible");
        }
    }
}
