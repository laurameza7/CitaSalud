package com.citasalud.exception;

/** El dato choca con otro que ya existe (correo repetido, franja ocupada). Se responde con 409. */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
