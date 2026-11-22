package com.citasalud.exception;

/** Correo o contraseña incorrectos, o cuenta desactivada. Se responde con 401. */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
