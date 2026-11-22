package com.citasalud.security;

import com.citasalud.model.Rol;

/** Datos del usuario que hizo la petición, sacados del token JWT. */
public record UsuarioActual(String id, String correo, Rol rol) {

    public boolean es(Rol otroRol) {
        return rol == otroRol;
    }
}
