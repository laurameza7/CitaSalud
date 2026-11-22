package com.citasalud.model.tarifa;

/**
 * Patrón Strategy (GRASP Polimorfismo): cada tipo de paciente calcula su valor a su manera.
 * RN_01: valor = tarifa − descuento + cargo administrativo.
 */
public interface EstrategiaTarifa {

    /** RN_03: cargo administrativo fijo por cita, en pesos. */
    long CARGO_ADMINISTRATIVO = 5000;

    /** Porcentaje de descuento como fracción: 0.15 significa 15 %. */
    double descuento();

    default long calcular(long tarifa) {
        long valorDescuento = Math.round(tarifa * descuento());
        return tarifa - valorDescuento + CARGO_ADMINISTRATIVO;
    }
}
