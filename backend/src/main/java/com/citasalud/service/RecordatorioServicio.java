package com.citasalud.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.citasalud.dto.RecordatorioResponse;
import com.citasalud.exception.AccesoDenegadoException;
import com.citasalud.exception.RecursoNoEncontradoException;
import com.citasalud.integracion.NotificadorRecordatorios;
import com.citasalud.model.Cita;
import com.citasalud.model.EstadoCita;
import com.citasalud.model.Recordatorio;
import com.citasalud.repository.CitaRepositorio;
import com.citasalud.repository.RecordatorioRepositorio;
import com.citasalud.security.UsuarioActual;

/**
 * Recordatorios de citas (HU_017, RF_019). Una tarea programada revisa las citas de las
 * próximas 24 horas y crea un recordatorio por cada una que aún no lo tenga (RN_13).
 */
@Service
public class RecordatorioServicio {

    private final RecordatorioRepositorio recordatorios;
    private final CitaRepositorio citas;
    private final NotificadorRecordatorios notificador;
    private final Clock reloj;

    public RecordatorioServicio(RecordatorioRepositorio recordatorios, CitaRepositorio citas,
                                NotificadorRecordatorios notificador, Clock reloj) {
        this.recordatorios = recordatorios;
        this.citas = citas;
        this.notificador = notificador;
        this.reloj = reloj;
    }

    /** Se ejecuta sola cada 15 minutos (app.recordatorios.intervalo-ms). Devuelve cuántos recordatorios creó. */
    @Scheduled(initialDelay = 60_000, fixedDelayString = "${app.recordatorios.intervalo-ms}")
    public int generarRecordatorios() {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        List<Cita> proximas = citas.findByEstadoAndFechaHoraBetween(EstadoCita.PROGRAMADA, ahora,
                ahora.plusHours(Cita.HORAS_ANTES_RECORDATORIO));
        int creados = 0;
        for (Cita cita : proximas) {
            if (cita.necesitaRecordatorio(ahora) && !recordatorios.existsByCitaAndFechaCita(cita, cita.getFechaHora())) {
                Recordatorio recordatorio = recordatorios.save(new Recordatorio(cita, ahora));
                notificador.enviar(recordatorio);
                creados++;
            }
        }
        return creados;
    }

    /** Recordatorios del paciente cuyas citas siguen vigentes, de la más cercana a la más lejana. */
    public List<RecordatorioResponse> misRecordatorios(UsuarioActual actual) {
        return recordatorios.findByPacienteIdOrderByFechaCitaAsc(actual.id()).stream()
                .filter(Recordatorio::estaVigente)
                .map(RecordatorioResponse::desde)
                .toList();
    }

    public RecordatorioResponse marcarLeido(String id, UsuarioActual actual) {
        Recordatorio recordatorio = recordatorios.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El recordatorio no existe"));
        if (!recordatorio.esDelPaciente(actual.id())) {
            throw new AccesoDenegadoException("No puedes modificar un recordatorio que no es tuyo");
        }
        recordatorio.marcarLeido();
        return RecordatorioResponse.desde(recordatorios.save(recordatorio));
    }
}
