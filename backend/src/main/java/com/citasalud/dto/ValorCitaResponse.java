package com.citasalud.dto;

import com.citasalud.model.TipoPaciente;

/** Desglose del valor de una cita (RF_010): tarifa − descuento + cargo administrativo = total. */
public record ValorCitaResponse(String especialidad, TipoPaciente tipoPaciente, long tarifa, int porcentajeDescuento,
                                long descuento, long cargoAdministrativo, long total) {
}
