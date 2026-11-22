package com.citasalud.service;

import org.springframework.stereotype.Service;

import com.citasalud.dto.UsuarioResponse;
import com.citasalud.exception.RecursoNoEncontradoException;
import com.citasalud.model.Paciente;
import com.citasalud.repository.UsuarioRepositorio;

/** Búsqueda de pacientes, usada por la recepcionista (HU_013). */
@Service
public class PacienteServicio {

    private final UsuarioRepositorio usuarios;

    public PacienteServicio(UsuarioRepositorio usuarios) {
        this.usuarios = usuarios;
    }

    public UsuarioResponse buscarPorDocumento(String documento) {
        return usuarios.buscarPacientePorDocumento(documento.trim())
                .map(UsuarioResponse::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un paciente con el documento " + documento));
    }

    Paciente buscarPaciente(String id) {
        return usuarios.findById(id)
                .filter(Paciente.class::isInstance)
                .map(Paciente.class::cast)
                .orElseThrow(() -> new RecursoNoEncontradoException("El paciente no existe"));
    }
}
