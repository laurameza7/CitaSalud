package com.citasalud.model.tarifa;

import com.citasalud.model.TipoPaciente;

/**
 * Patrón Simple Factory (GRASP Fabricación pura): entrega la estrategia de tarifa
 * que corresponde a cada tipo de paciente.
 * Para agregar un tipo nuevo: valor en TipoPaciente + clase Tarifa... + un caso aquí.
 */
public final class FabricaTarifas {

    private FabricaTarifas() {
    }

    public static EstrategiaTarifa para(TipoPaciente tipo) {
        return switch (tipo) {
            case PARTICULAR -> new TarifaParticular();
            case CONVENIO -> new TarifaConvenio();
            case PREPAGADA -> new TarifaPrepagada();
        };
    }
}
