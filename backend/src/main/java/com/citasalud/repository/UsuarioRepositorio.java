package com.citasalud.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.citasalud.model.Rol;
import com.citasalud.model.Usuario;

/** Patrón Repository: acceso a la colección "usuarios" (todos los roles). */
public interface UsuarioRepositorio extends MongoRepository<Usuario, String> {

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    List<Usuario> findByRolOrderByNombreAsc(Rol rol);

    @Query("{ 'rol': 'PACIENTE', 'documento': ?0 }")
    Optional<Usuario> buscarPacientePorDocumento(String documento);

    @Query(value = "{ 'rol': 'PACIENTE', 'documento': ?0 }", exists = true)
    boolean existePacienteConDocumento(String documento);
}
