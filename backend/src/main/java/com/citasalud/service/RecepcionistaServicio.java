package com.citasalud.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.citasalud.dto.RecepcionistaRequest;
import com.citasalud.dto.UsuarioResponse;
import com.citasalud.exception.ConflictoException;
import com.citasalud.exception.RecursoNoEncontradoException;
import com.citasalud.model.Recepcionista;
import com.citasalud.model.Rol;
import com.citasalud.model.Usuario;
import com.citasalud.repository.UsuarioRepositorio;

/** Gestión de cuentas de recepcionista (HU_005). */
@Service
public class RecepcionistaServicio {

    private final UsuarioRepositorio usuarios;
    private final PasswordEncoder codificador;

    public RecepcionistaServicio(UsuarioRepositorio usuarios, PasswordEncoder codificador) {
        this.usuarios = usuarios;
        this.codificador = codificador;
    }

    public List<UsuarioResponse> listar() {
        return usuarios.findByRolOrderByNombreAsc(Rol.RECEPCIONISTA).stream().map(UsuarioResponse::desde).toList();
    }

    public UsuarioResponse crear(RecepcionistaRequest datos) {
        String correo = AuthServicio.normalizarCorreo(datos.correo());
        if (usuarios.existsByCorreo(correo)) {
            throw new ConflictoException("El correo " + correo + " ya está en uso");
        }
        Recepcionista recepcionista = new Recepcionista(datos.nombre().trim(), correo,
                codificador.encode(datos.contrasena()), datos.telefono().trim());
        return UsuarioResponse.desde(usuarios.save(recepcionista));
    }

    public void desactivar(String id) {
        Usuario usuario = usuarios.findById(id)
                .filter(Recepcionista.class::isInstance)
                .orElseThrow(() -> new RecursoNoEncontradoException("La recepcionista no existe"));
        usuario.desactivar();
        usuarios.save(usuario);
    }
}
