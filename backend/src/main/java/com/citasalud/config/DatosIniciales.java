package com.citasalud.config;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.citasalud.model.Administrador;
import com.citasalud.model.Cita;
import com.citasalud.model.Especialidad;
import com.citasalud.model.EstadoCita;
import com.citasalud.model.HorarioAtencion;
import com.citasalud.model.Medico;
import com.citasalud.model.Paciente;
import com.citasalud.model.Recepcionista;
import com.citasalud.model.TipoPaciente;
import com.citasalud.repository.CitaRepositorio;
import com.citasalud.repository.EspecialidadRepositorio;
import com.citasalud.repository.UsuarioRepositorio;

/**
 * Carga datos de prueba la primera vez que arranca la aplicación con la base vacía.
 * Se desactiva con la variable DATOS_PRUEBA=false.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final UsuarioRepositorio usuarios;
    private final EspecialidadRepositorio especialidades;
    private final CitaRepositorio citas;
    private final PasswordEncoder codificador;
    private final Clock reloj;
    private final boolean activo;

    public DatosIniciales(UsuarioRepositorio usuarios, EspecialidadRepositorio especialidades, CitaRepositorio citas,
                          PasswordEncoder codificador, Clock reloj, @Value("${app.datos-prueba}") boolean activo) {
        this.usuarios = usuarios;
        this.especialidades = especialidades;
        this.citas = citas;
        this.codificador = codificador;
        this.reloj = reloj;
        this.activo = activo;
    }

    @Override
    public void run(String... args) {
        if (!activo || usuarios.count() > 0) {
            return;
        }
        log.info("Base de datos vacía: cargando datos de prueba de CitaSalud");

        usuarios.save(new Administrador("Administración CitaSalud", "admin@citasalud.com", clave("Admin123*"), "3001112233"));
        usuarios.save(new Recepcionista("Marta Gómez", "recepcion@citasalud.com", clave("Recepcion123*"), "3002223344"));

        Especialidad general = especialidades.save(new Especialidad("Medicina general",
                "Consulta de valoración general y control", 50000));
        Especialidad pediatria = especialidades.save(new Especialidad("Pediatría",
                "Atención de niños y adolescentes", 80000));
        Especialidad dermatologia = especialidades.save(new Especialidad("Dermatología",
                "Enfermedades de la piel, el cabello y las uñas", 120000));
        Especialidad cardiologia = especialidades.save(new Especialidad("Cardiología",
                "Enfermedades del corazón y la presión arterial", 150000));

        Medico carlos = medico("Carlos Ruiz", "carlos.ruiz@citasalud.com", "RM-10234", general, "Consultorio 101",
                semana(LocalTime.of(8, 0), LocalTime.of(12, 0)));
        medico("Laura Benavides", "laura.benavides@citasalud.com", "RM-11876", general, "Consultorio 102",
                semana(LocalTime.of(14, 0), LocalTime.of(18, 0)));
        Medico andres = medico("Andrés Ortega", "andres.ortega@citasalud.com", "RM-20451", pediatria, "Consultorio 201",
                semana(LocalTime.of(7, 0), LocalTime.of(11, 0)));
        Medico sofia = medico("Sofía Delgado", "sofia.delgado@citasalud.com", "RM-30987", dermatologia, "Consultorio 301",
                List.of(new HorarioAtencion(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(13, 0)),
                        new HorarioAtencion(DayOfWeek.THURSDAY, LocalTime.of(14, 0), LocalTime.of(18, 0))));
        Medico jorge = medico("Jorge Erazo", "jorge.erazo@citasalud.com", "RM-40112", cardiologia, "Consultorio 401",
                List.of(new HorarioAtencion(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0)),
                        new HorarioAtencion(DayOfWeek.WEDNESDAY, LocalTime.of(8, 0), LocalTime.of(12, 0))));

        Paciente ana = usuarios.save(new Paciente("Ana Pérez", "ana.perez@correo.com", clave("Paciente123*"), "3104445566",
                "1085000111", LocalDate.of(1998, 3, 14), TipoPaciente.CONVENIO));
        Paciente luis = usuarios.save(new Paciente("Luis Martínez", "luis.martinez@correo.com", clave("Paciente123*"),
                "3115556677", "1085000222", LocalDate.of(1985, 9, 2), TipoPaciente.PARTICULAR));
        Paciente camila = usuarios.save(new Paciente("Camila Rosero", "camila.rosero@correo.com", clave("Paciente123*"),
                "3126667788", "1085000333", LocalDate.of(2001, 11, 25), TipoPaciente.PREPAGADA));

        // Algunas citas de ejemplo: pasadas (para el reporte) y futuras (para cancelar o reprogramar).
        LocalDate hoy = LocalDate.now(reloj);
        LocalDate lunesPasado = hoy.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
        LocalDate proximoMartes = hoy.plusDays(2).with(TemporalAdjusters.next(DayOfWeek.TUESDAY));
        LocalDate proximoMiercoles = hoy.plusDays(2).with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY));

        cita(ana, carlos, lunesPasado.atTime(8, 0), "Control general", EstadoCita.ATENDIDA);
        cita(luis, carlos, lunesPasado.atTime(8, 30), "Dolor de cabeza", EstadoCita.NO_ASISTIO);
        cita(camila, sofia, proximoMartes.atTime(9, 0), "Revisión de lunar", EstadoCita.PROGRAMADA);
        cita(ana, andres, proximoMiercoles.atTime(7, 30), "Control de crecimiento de mi hijo", EstadoCita.PROGRAMADA);
        // Una cita de hoy que ya empezó, para mostrar en vivo cómo el médico registra la atención (HU_015).
        if (hoy.getDayOfWeek() != DayOfWeek.SATURDAY && hoy.getDayOfWeek() != DayOfWeek.SUNDAY) {
            cita(luis, carlos, hoy.atTime(8, 0), "Chequeo anual", EstadoCita.PROGRAMADA);
        }
        // Una cita de Ana dentro de las próximas 24 horas, para que aparezca su recordatorio (HU_017).
        LocalDateTime ahora = LocalDateTime.now(reloj);
        LocalDateTime proximaCita = ahora.getHour() < 8 ? hoy.atTime(10, 0) : hoy.plusDays(1).atTime(8, 0);
        cita(ana, jorge, proximaCita, "Control de presión arterial", EstadoCita.PROGRAMADA);
        log.info("Datos de prueba cargados");
    }

    private String clave(String plana) {
        return codificador.encode(plana);
    }

    private Medico medico(String nombre, String correo, String registro, Especialidad especialidad, String consultorio,
                          List<HorarioAtencion> horarios) {
        Medico medico = new Medico(nombre, correo, clave("Medico123*"), "3010000000", registro, especialidad, consultorio);
        medico.definirHorarios(horarios);
        return usuarios.save(medico);
    }

    private List<HorarioAtencion> semana(LocalTime inicio, LocalTime fin) {
        List<HorarioAtencion> horarios = new ArrayList<>();
        for (DayOfWeek dia : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
            horarios.add(new HorarioAtencion(dia, inicio, fin));
        }
        return horarios;
    }

    private void cita(Paciente paciente, Medico medico, LocalDateTime fechaHora, String motivo, EstadoCita estado) {
        long valor = medico.getEspecialidad().valorPara(paciente.getTipoPaciente());
        Cita cita = new Cita(paciente, medico, medico.getEspecialidad(), fechaHora, motivo, valor,
                fechaHora.minusDays(3));
        cita.registrarPago("PAGO-DEMO" + (int) (Math.random() * 9000 + 1000));
        cita.setEstado(estado);
        if (estado == EstadoCita.ATENDIDA) {
            cita.setNotasAtencion("Paciente en buen estado general. Control en 6 meses.");
        }
        citas.save(cita);
    }
}
