package com.citasalud.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.citasalud.dto.LoginRequest;
import com.citasalud.dto.LoginResponse;
import com.citasalud.dto.RegistroPacienteRequest;
import com.citasalud.dto.UsuarioResponse;
import com.citasalud.exception.ConflictoException;
import com.citasalud.exception.CredencialesInvalidasException;
import com.citasalud.exception.RecursoNoEncontradoException;
import com.citasalud.model.Paciente;
import com.citasalud.model.Usuario;
import com.citasalud.repository.UsuarioRepositorio;
import com.citasalud.security.JwtServicio;

/** Registro de pacientes e inicio de sesión (HU_001, HU_002). */
@Service
public class AuthServicio {

    private final UsuarioRepositorio usuarios;
    private final PasswordEncoder codificador;
    private final JwtServicio jwtServicio;

    public AuthServicio(UsuarioRepositorio usuarios, PasswordEncoder codificador, JwtServicio jwtServicio) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.jwtServicio = jwtServicio;
    }

    public UsuarioResponse registrar(RegistroPacienteRequest datos) {
        String correo = normalizarCorreo(datos.correo());
        if (usuarios.existsByCorreo(correo)) {
            throw new ConflictoException("El correo " + correo + " ya está en uso");
        }
        if (usuarios.existePacienteConDocumento(datos.documento().trim())) {
            throw new ConflictoException("Ya existe un paciente con el documento " + datos.documento().trim());
        }
        Paciente paciente = new Paciente(datos.nombre().trim(), correo, codificador.encode(datos.contrasena()),
                datos.telefono().trim(), datos.documento().trim(), datos.fechaNacimiento(), datos.tipoPaciente());
        return UsuarioResponse.desde(usuarios.save(paciente));
    }

    public LoginResponse login(LoginRequest datos) {
        Usuario usuario = usuarios.findByCorreo(normalizarCorreo(datos.correo()))
                .filter(Usuario::isActivo)
                .filter(u -> u.verificarContrasena(datos.contrasena(), codificador))
                .orElseThrow(() -> new CredencialesInvalidasException("El correo o la contraseña no son válidos"));
        return new LoginResponse(jwtServicio.generar(usuario), UsuarioResponse.desde(usuario));
    }

    public UsuarioResponse yo(String usuarioId) {
        return usuarios.findById(usuarioId).map(UsuarioResponse::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario no existe"));
    }

    static String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase();
    }
}
