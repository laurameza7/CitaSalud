package com.citasalud.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.citasalud.Fixtures;
import com.citasalud.dto.RecordatorioResponse;
import com.citasalud.exception.AccesoDenegadoException;
import com.citasalud.integracion.NotificadorRecordatorios;
import com.citasalud.model.Cita;
import com.citasalud.model.EstadoCita;
import com.citasalud.model.Medico;
import com.citasalud.model.Paciente;
import com.citasalud.model.Recordatorio;
import com.citasalud.model.Rol;
import com.citasalud.model.TipoPaciente;
import com.citasalud.repository.CitaRepositorio;
import com.citasalud.repository.RecordatorioRepositorio;
import com.citasalud.security.UsuarioActual;

/** HU_017 y RN_13 con Mockito. "Ahora" es el domingo anterior a la cita, a las 10:00. */
class RecordatorioServicioTest {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    private final LocalDateTime ahora = Fixtures.LUNES.minusDays(1).atTime(10, 0);

    private RecordatorioRepositorio recordatorios;
    private CitaRepositorio citas;
    private NotificadorRecordatorios notificador;
    private RecordatorioServicio servicio;

    private Medico medico;
    private Paciente paciente;
    private Cita cita;
    private final UsuarioActual actual = new UsuarioActual("p1", "p1@correo.com", Rol.PACIENTE);

    @BeforeEach
    void preparar() {
        recordatorios = mock(RecordatorioRepositorio.class);
        citas = mock(CitaRepositorio.class);
        notificador = mock(NotificadorRecordatorios.class);
        Clock reloj = Clock.fixed(ahora.atZone(BOGOTA).toInstant(), BOGOTA);
        servicio = new RecordatorioServicio(recordatorios, citas, notificador, reloj);

        medico = Fixtures.medico("m1", Fixtures.especialidad("Medicina general", 60000));
        paciente = Fixtures.paciente("p1", TipoPaciente.PARTICULAR);
        // La cita se agendó días antes; mañana lunes a las 09:00 (faltan 23 horas).
        cita = medico.crearCita(paciente, Fixtures.LUNES.atTime(9, 0), "Control", ahora.minusDays(3));
        cita.setId("c1");
        when(recordatorios.save(any(Recordatorio.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("Crea y envía un recordatorio para la cita de mañana")
    void generaRecordatorio() {
        when(citas.findByEstadoAndFechaHoraBetween(EstadoCita.PROGRAMADA, ahora, ahora.plusHours(24)))
                .thenReturn(List.of(cita));

        assertEquals(1, servicio.generarRecordatorios());
        verify(notificador).enviar(any(Recordatorio.class));
    }

    @Test
    @DisplayName("No repite el recordatorio si la cita ya tiene uno")
    void noDuplica() {
        when(citas.findByEstadoAndFechaHoraBetween(EstadoCita.PROGRAMADA, ahora, ahora.plusHours(24)))
                .thenReturn(List.of(cita));
        when(recordatorios.existsByCitaAndFechaCita(cita, cita.getFechaHora())).thenReturn(true);

        assertEquals(0, servicio.generarRecordatorios());
        verify(recordatorios, never()).save(any());
        verify(notificador, never()).enviar(any());
    }

    @Test
    @DisplayName("El mensaje dice especialidad, médico, fecha, hora y consultorio")
    void mensaje() {
        Recordatorio r = new Recordatorio(cita, ahora);
        assertEquals("Recuerda tu cita de Medicina general con Dr. m1 el 30/11/2026 a las 09:00. Lugar: 101.",
                r.getMensaje());
    }

    @Test
    @DisplayName("Una cita cancelada deja de mostrar su recordatorio")
    void canceladaNoSeMuestra() {
        Recordatorio r = new Recordatorio(cita, ahora);
        r.setId("r1");
        when(recordatorios.findByPacienteIdOrderByFechaCitaAsc("p1")).thenReturn(List.of(r));
        assertEquals(1, servicio.misRecordatorios(actual).size());

        // Otra cita del paciente que se canceló a tiempo, días antes.
        Cita otra = medico.crearCita(paciente, Fixtures.LUNES.atTime(9, 30), "", ahora.minusDays(3));
        otra.cancelar(ahora.minusDays(3));
        Recordatorio deCancelada = new Recordatorio(otra, ahora);
        when(recordatorios.findByPacienteIdOrderByFechaCitaAsc("p1")).thenReturn(List.of(deCancelada));
        assertTrue(servicio.misRecordatorios(actual).isEmpty());
    }

    @Test
    @DisplayName("Si la cita se reprograma, el recordatorio viejo deja de estar vigente")
    void reprogramadaInvalidaElAnterior() {
        Cita futura = medico.crearCita(paciente, Fixtures.LUNES.plusDays(7).atTime(8, 0), "", ahora);
        Recordatorio viejo = new Recordatorio(futura, ahora);
        futura.reprogramar(Fixtures.LUNES.plusDays(7).atTime(9, 0), ahora);
        assertFalse(viejo.estaVigente());
    }

    @Test
    @DisplayName("La cita necesita recordatorio solo dentro de las 24 horas previas")
    void ventanaDe24Horas() {
        assertTrue(cita.necesitaRecordatorio(ahora));
        assertFalse(cita.necesitaRecordatorio(ahora.minusDays(2)));
        assertFalse(cita.necesitaRecordatorio(Fixtures.LUNES.atTime(9, 30)));
    }

    @Test
    @DisplayName("El paciente marca su recordatorio como leído; otro paciente no puede")
    void marcarLeido() {
        Recordatorio r = new Recordatorio(cita, ahora);
        r.setId("r1");
        when(recordatorios.findById("r1")).thenReturn(Optional.of(r));

        RecordatorioResponse respuesta = servicio.marcarLeido("r1", actual);
        assertTrue(respuesta.leido());
        verify(recordatorios, times(1)).save(eq(r));

        UsuarioActual otro = new UsuarioActual("p2", "p2@correo.com", Rol.PACIENTE);
        assertThrows(AccesoDenegadoException.class, () -> servicio.marcarLeido("r1", otro));
    }
}
