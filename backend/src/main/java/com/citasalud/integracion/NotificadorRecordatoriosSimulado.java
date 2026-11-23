package com.citasalud.integracion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.citasalud.model.Recordatorio;

/** Notificador del proyecto: escribe el recordatorio en el log en vez de enviar un correo real. */
@Component
public class NotificadorRecordatoriosSimulado implements NotificadorRecordatorios {

    private static final Logger LOG = LoggerFactory.getLogger(NotificadorRecordatoriosSimulado.class);

    @Override
    public void enviar(Recordatorio recordatorio) {
        LOG.info("Recordatorio para el paciente {}: {}", recordatorio.getPacienteId(), recordatorio.getMensaje());
    }
}
