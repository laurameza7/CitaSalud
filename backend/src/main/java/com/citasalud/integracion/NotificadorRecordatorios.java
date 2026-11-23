package com.citasalud.integracion;

import com.citasalud.model.Recordatorio;

/**
 * Canal por el que sale el recordatorio (correo, SMS, push...). GRASP Variaciones protegidas:
 * igual que con la pasarela de pagos, cambiar de canal no toca la lógica de recordatorios.
 */
public interface NotificadorRecordatorios {

    void enviar(Recordatorio recordatorio);
}
