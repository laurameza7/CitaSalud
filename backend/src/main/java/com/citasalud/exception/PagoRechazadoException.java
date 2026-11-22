package com.citasalud.exception;

/** La pasarela de pagos rechazó el cobro (RN_09). Se responde con 402. */
public class PagoRechazadoException extends RuntimeException {

    public PagoRechazadoException(String mensaje) {
        super(mensaje);
    }
}
