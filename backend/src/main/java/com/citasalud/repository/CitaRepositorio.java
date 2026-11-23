package com.citasalud.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.citasalud.model.Cita;
import com.citasalud.model.Especialidad;
import com.citasalud.model.EstadoCita;
import com.citasalud.model.Medico;
import com.citasalud.model.Paciente;

/** Patrón Repository: acceso a la colección "citas". Spring genera las consultas por el nombre del método. */
public interface CitaRepositorio extends MongoRepository<Cita, String> {

    List<Cita> findByMedicoAndEstadoAndFechaHoraBetween(Medico medico, EstadoCita estado,
                                                        LocalDateTime desde, LocalDateTime hasta);

    boolean existsByMedicoAndFechaHoraAndEstado(Medico medico, LocalDateTime fechaHora, EstadoCita estado);

    boolean existsByPacienteAndFechaHoraAndEstado(Paciente paciente, LocalDateTime fechaHora, EstadoCita estado);

    boolean existsByPacienteAndEspecialidadAndEstado(Paciente paciente, Especialidad especialidad, EstadoCita estado);

    List<Cita> findByPacienteOrderByFechaHoraDesc(Paciente paciente);

    List<Cita> findByMedicoAndFechaHoraBetweenOrderByFechaHoraAsc(Medico medico, LocalDateTime desde, LocalDateTime hasta);

    List<Cita> findByFechaHoraBetween(LocalDateTime desde, LocalDateTime hasta);

    List<Cita> findByEstadoAndFechaHoraBetween(EstadoCita estado, LocalDateTime desde, LocalDateTime hasta);
}
