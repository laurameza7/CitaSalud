package com.citasalud.exception;

/** Se incumple una regla de negocio (RN). Se responde con 422. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
