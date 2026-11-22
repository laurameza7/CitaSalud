package com.citasalud.exception;

/** El usuario no puede actuar sobre ese recurso, por ejemplo la cita de otro paciente. Se responde con 403. */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
