package com.citasalud.model;

/** Roles de los usuarios con cuenta. El visitante no tiene rol porque no tiene cuenta. */
public enum Rol {
    PACIENTE,
    MEDICO,
    RECEPCIONISTA,
    ADMINISTRADOR
}
