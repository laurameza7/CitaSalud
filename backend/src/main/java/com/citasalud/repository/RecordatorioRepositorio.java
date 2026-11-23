package com.citasalud.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.citasalud.model.Cita;
import com.citasalud.model.Recordatorio;

/** Patrón Repository: acceso a la colección "recordatorios". */
public interface RecordatorioRepositorio extends MongoRepository<Recordatorio, String> {

    boolean existsByCitaAndFechaCita(Cita cita, LocalDateTime fechaCita);

    List<Recordatorio> findByPacienteIdOrderByFechaCitaAsc(String pacienteId);
}
