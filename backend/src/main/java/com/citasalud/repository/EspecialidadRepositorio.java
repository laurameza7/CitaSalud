package com.citasalud.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.citasalud.model.Especialidad;

/** Patrón Repository: acceso a la colección "especialidades". */
public interface EspecialidadRepositorio extends MongoRepository<Especialidad, String> {

    List<Especialidad> findByActivaTrueOrderByNombreAsc();

    List<Especialidad> findAllByOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);
}
