package com.citasalud.model.tarifa;

/** Paciente con medicina prepagada. RN_02. */
public class TarifaPrepagada implements EstrategiaTarifa {

    public static final double DESCUENTO = 0.30;

    @Override
    public double descuento() {
        return DESCUENTO;
    }
}
