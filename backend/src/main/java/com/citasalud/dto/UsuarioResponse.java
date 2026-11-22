package com.citasalud.dto;

import java.time.LocalDate;

import com.citasalud.model.Paciente;
import com.citasalud.model.Rol;
import com.citasalud.model.TipoPaciente;
import com.citasalud.model.Usuario;

/** Datos públicos de un usuario (nunca incluye la contraseña). Los campos de paciente van vacíos en otros roles. */
public record UsuarioResponse(String id, String nombre, String correo, String telefono, Rol rol, boolean activo,
                              String documento, LocalDate fechaNacimiento, TipoPaciente tipoPaciente) {

    public static UsuarioResponse desde(Usuario u) {
        if (u instanceof Paciente p) {
            return new UsuarioResponse(p.getId(), p.getNombre(), p.getCorreo(), p.getTelefono(), p.getRol(), p.isActivo(),
                    p.getDocumento(), p.getFechaNacimiento(), p.getTipoPaciente());
        }
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getCorreo(), u.getTelefono(), u.getRol(), u.isActivo(),
                null, null, null);
    }
}
