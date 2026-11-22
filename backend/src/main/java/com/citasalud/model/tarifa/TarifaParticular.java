package com.citasalud.model.tarifa;

/** Paciente que paga la consulta completa. RN_02. */
public class TarifaParticular implements EstrategiaTarifa {

    public static final double DESCUENTO = 0.0;

    @Override
    public double descuento() {
        return DESCUENTO;
    }
}
