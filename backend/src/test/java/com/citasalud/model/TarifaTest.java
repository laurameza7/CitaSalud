package com.citasalud.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.citasalud.Fixtures;
import com.citasalud.model.tarifa.FabricaTarifas;
import com.citasalud.model.tarifa.TarifaConvenio;
import com.citasalud.model.tarifa.TarifaParticular;
import com.citasalud.model.tarifa.TarifaPrepagada;

/** RN_01, RN_02 y RN_03 con una tarifa de $60.000. */
class TarifaTest {

    private final Especialidad especialidad = Fixtures.especialidad("Medicina general", 60000);

    @Test
    @DisplayName("Particular paga la tarifa completa más el cargo administrativo")
    void particular() {
        assertEquals(65000, especialidad.valorPara(TipoPaciente.PARTICULAR));
    }

    @Test
    @DisplayName("Convenio tiene 15 % de descuento")
    void convenio() {
        assertEquals(56000, especialidad.valorPara(TipoPaciente.CONVENIO));
    }

    @Test
    @DisplayName("Prepagada tiene 30 % de descuento")
    void prepagada() {
        assertEquals(47000, especialidad.valorPara(TipoPaciente.PREPAGADA));
    }

    @Test
    @DisplayName("La fábrica entrega la estrategia de cada tipo")
    void fabrica() {
        assertInstanceOf(TarifaParticular.class, FabricaTarifas.para(TipoPaciente.PARTICULAR));
        assertInstanceOf(TarifaConvenio.class, FabricaTarifas.para(TipoPaciente.CONVENIO));
        assertInstanceOf(TarifaPrepagada.class, FabricaTarifas.para(TipoPaciente.PREPAGADA));
    }
}
