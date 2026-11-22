package com.citasalud.integracion;

/** Respuesta de la pasarela de pagos. */
public record ResultadoPago(boolean aprobado, String referencia, String mensaje) {
}
