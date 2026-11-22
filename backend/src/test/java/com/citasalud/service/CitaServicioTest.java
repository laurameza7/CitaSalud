package com.citasalud.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.citasalud.Fixtures;
import com.citasalud.dto.AgendarCitaRequest;
import com.citasalud.dto.CitaResponse;
import com.citasalud.exception.AccesoDenegadoException;
import com.citasalud.exception.ConflictoException;
import com.citasalud.exception.PagoRechazadoException;
import com.citasalud.integracion.PasarelaPagos;
import com.citasalud.integracion.ResultadoPago;
import com.citasalud.model.Cita;
import com.citasalud.model.EstadoCita;
import com.citasalud.model.Medico;
import com.citasalud.model.Paciente;
import com.citasalud.model.Rol;
import com.citasalud.model.TipoPaciente;
import com.citasalud.repository.CitaRepositorio;
import com.citasalud.security.UsuarioActual;

/** HU_009 y HU_011 con Mockito: sin base de datos y con la hora fija. */
class CitaServicioTest {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    private CitaRepositorio citas;
    private MedicoServicio medicoServicio;
    private PacienteServicio pacienteServicio;
    private DisponibilidadServicio disponibilidad;
    private PasarelaPagos pasarela;
    private CitaServicio servicio;

    private Medico medico;
    private Paciente paciente;
    private final UsuarioActual actual = new UsuarioActual("p1", "p1@correo.com", Rol.PACIENTE);

    @BeforeEach
    void preparar() {
        citas = mock(CitaRepositorio.class);
        medicoServicio = mock(MedicoServicio.class);
        pacienteServicio = mock(PacienteServicio.class);
        disponibilidad = mock(DisponibilidadServicio.class);
        pasarela = mock(PasarelaPagos.class);
        // "Ahora" es el jueves anterior al lunes de la cita, a las 10:00.
        Clock reloj = Clock.fixed(Fixtures.LUNES.minusDays(4).atTime(10, 0).atZone(BOGOTA).toInstant(), BOGOTA);
        servicio = new CitaServicio(citas, medicoServicio, pacienteServicio, disponibilidad, pasarela, reloj);

        medico = Fixtures.medico("m1", Fixtures.especialidad("Medicina general", 60000));
        paciente = Fixtures.paciente("p1", TipoPaciente.CONVENIO);
        when(medicoServicio.buscarMedico("m1")).thenReturn(medico);
        when(pacienteServicio.buscarPaciente("p1")).thenReturn(paciente);
        when(citas.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private AgendarCitaRequest peticion(String tarjeta) {
        return new AgendarCitaRequest("m1", Fixtures.LUNES.atTime(9, 0), "Control", tarjeta, null);
    }

    @Test
    @DisplayName("Agenda la cita y cobra 56.000 a un paciente de convenio")
    void agendarExitoso() {
        when(pasarela.cobrar(anyString(), anyLong(), anyString())).thenReturn(new ResultadoPago(true, "PAGO-1", "ok"));

        CitaResponse respuesta = servicio.agendar(peticion("4111111111111111"), actual);

        assertEquals(EstadoCita.PROGRAMADA, respuesta.estado());
        assertEquals(56000, respuesta.valorPagado());
        assertEquals("PAGO-1", respuesta.referenciaPago());
        verify(pasarela).cobrar("4111111111111111", 56000, "Cita de Medicina general");
    }

    @Test
    @DisplayName("Si la pasarela rechaza el pago, la cita no se guarda")
    void pagoRechazado() {
        when(pasarela.cobrar(anyString(), anyLong(), anyString())).thenReturn(new ResultadoPago(false, null, "rechazado"));

        assertThrows(PagoRechazadoException.class, () -> servicio.agendar(peticion("4111111111110000"), actual));
        verify(citas, never()).save(any());
    }

    @Test
    @DisplayName("Si la franja está ocupada, no se cobra ni se guarda")
    void franjaOcupada() {
        doThrow(new ConflictoException("ocupada")).when(disponibilidad).verificarFranjaLibre(any(), any());

        assertThrows(ConflictoException.class, () -> servicio.agendar(peticion("4111111111111111"), actual));
        verify(pasarela, never()).cobrar(anyString(), anyLong(), anyString());
        verify(citas, never()).save(any());
    }

    @Test
    @DisplayName("No agenda dos citas programadas en la misma especialidad")
    void mismaEspecialidad() {
        when(citas.existsByPacienteAndEspecialidadAndEstado(paciente, medico.getEspecialidad(), EstadoCita.PROGRAMADA))
                .thenReturn(true);

        assertThrows(ConflictoException.class, () -> servicio.agendar(peticion("4111111111111111"), actual));
        verify(pasarela, never()).cobrar(anyString(), anyLong(), anyString());
    }

    @Test
    @DisplayName("Al cancelar a tiempo se pide el reembolso")
    void cancelarConReembolso() {
        Cita cita = medico.crearCita(paciente, Fixtures.LUNES.atTime(9, 0), "Control", Fixtures.LUNES.minusDays(5).atTime(8, 0));
        cita.setId("c1");
        cita.registrarPago("PAGO-1");
        when(citas.findById("c1")).thenReturn(Optional.of(cita));
        when(pasarela.reembolsar("PAGO-1", 56000)).thenReturn(true);

        CitaResponse respuesta = servicio.cancelar("c1", actual);

        assertEquals(EstadoCita.CANCELADA, respuesta.estado());
        assertTrue(respuesta.reembolsada());
    }

    @Test
    @DisplayName("Un paciente no puede cancelar la cita de otro")
    void cancelarCitaAjena() {
        Cita cita = medico.crearCita(paciente, Fixtures.LUNES.atTime(9, 0), "Control", Fixtures.LUNES.minusDays(5).atTime(8, 0));
        when(citas.findById("c1")).thenReturn(Optional.of(cita));
        UsuarioActual otro = new UsuarioActual("p2", "p2@correo.com", Rol.PACIENTE);

        assertThrows(AccesoDenegadoException.class, () -> servicio.cancelar("c1", otro));
    }
}
