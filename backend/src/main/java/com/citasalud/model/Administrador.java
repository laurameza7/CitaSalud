package com.citasalud.model;

import org.springframework.data.mongodb.core.mapping.Document;

/** Gestiona especialidades, médicos y recepcionistas, y consulta reportes. */
@Document(collection = "usuarios")
public class Administrador extends Usuario {

    protected Administrador() {
    }

    public Administrador(String nombre, String correo, String contrasenaHash, String telefono) {
        super(nombre, correo, contrasenaHash, telefono, Rol.ADMINISTRADOR);
    }
}
