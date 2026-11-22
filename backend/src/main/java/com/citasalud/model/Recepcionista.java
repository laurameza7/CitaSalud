package com.citasalud.model;

import org.springframework.data.mongodb.core.mapping.Document;

/** Personal de la clínica que agenda, cancela y reprograma citas en nombre de los pacientes. */
@Document(collection = "usuarios")
public class Recepcionista extends Usuario {

    protected Recepcionista() {
    }

    public Recepcionista(String nombre, String correo, String contrasenaHash, String telefono) {
        super(nombre, correo, contrasenaHash, telefono, Rol.RECEPCIONISTA);
    }
}
