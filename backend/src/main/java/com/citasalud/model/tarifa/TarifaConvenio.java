package com.citasalud.model.tarifa;

/** Paciente de una empresa con convenio con la clínica. RN_02. */
public class TarifaConvenio implements EstrategiaTarifa {

    public static final double DESCUENTO = 0.15;

    @Override
    public double descuento() {
        return DESCUENTO;
    }
}
