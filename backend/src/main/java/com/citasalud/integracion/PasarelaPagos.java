package com.citasalud.integracion;

/**
 * Interfaz propia hacia la pasarela de pagos externa (RNF_012).
 * GRASP Variaciones protegidas / Indirección: si mañana se usa una pasarela real,
 * solo se cambia la implementación y la lógica de citas no se toca.
 */
public interface PasarelaPagos {

    ResultadoPago cobrar(String numeroTarjeta, long valor, String concepto);

    boolean reembolsar(String referencia, long valor);
}
