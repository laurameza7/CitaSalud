package com.citasalud.integracion;

import java.util.UUID;

import org.springframework.stereotype.Component;

/**
 * Pasarela simulada para el proyecto: aprueba cualquier tarjeta de 16 dígitos
 * que no termine en 0000. Así se puede probar el caso de pago rechazado.
 */
@Component
public class PasarelaPagosSimulada implements PasarelaPagos {

    @Override
    public ResultadoPago cobrar(String numeroTarjeta, long valor, String concepto) {
        String tarjeta = numeroTarjeta == null ? "" : numeroTarjeta.replace(" ", "");
        if (!tarjeta.matches("\\d{16}")) {
            return new ResultadoPago(false, null, "La tarjeta debe tener 16 dígitos");
        }
        if (tarjeta.endsWith("0000")) {
            return new ResultadoPago(false, null, "La entidad financiera rechazó el pago");
        }
        String referencia = "PAGO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new ResultadoPago(true, referencia, "Pago aprobado");
    }

    @Override
    public boolean reembolsar(String referencia, long valor) {
        return referencia != null;
    }
}
