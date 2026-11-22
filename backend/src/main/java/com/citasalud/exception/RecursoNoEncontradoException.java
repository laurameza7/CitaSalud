package com.citasalud.exception;

/** No existe el recurso pedido. Se responde con 404. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
