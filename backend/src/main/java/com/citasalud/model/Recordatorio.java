package com.citasalud.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Recordatorio de una cita próxima (HU_017). Se crea uno por cita y por fecha:
 * si la cita se reprograma, la nueva fecha recibe su propio recordatorio (RN_13).
 */
@Document(collection = "recordatorios")
public class Recordatorio {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Id
    private String id;
    @DBRef
    private Cita cita;
    @Indexed
    private String pacienteId;
    private LocalDateTime fechaCita;
    private String mensaje;
    private LocalDateTime creadoEn;
    private boolean leido;

    protected Recordatorio() {
    }

    /** Se construye con los datos de la cita que recuerda. */
    public Recordatorio(Cita cita, LocalDateTime creadoEn) {
        this.cita = cita;
        this.pacienteId = cita.getPaciente().getId();
        this.fechaCita = cita.getFechaHora();
        this.creadoEn = creadoEn;
        this.mensaje = "Recuerda tu cita de " + cita.getEspecialidad().getNombre() + " con "
                + cita.getMedico().getNombre() + " el " + cita.getFechaHora().format(FORMATO_FECHA) + " a las "
                + cita.getFechaHora().toLocalTime() + ". Lugar: " + cita.getMedico().getConsultorio() + ".";
    }

    /** RN_13: solo se muestra si la cita sigue programada para la misma fecha. */
    public boolean estaVigente() {
        return cita != null && cita.estaProgramada() && cita.getFechaHora().equals(fechaCita);
    }

    public boolean esDelPaciente(String id) {
        return pacienteId != null && pacienteId.equals(id);
    }

    public void marcarLeido() {
        this.leido = true;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Cita getCita() { return cita; }
    public String getPacienteId() { return pacienteId; }
    public LocalDateTime getFechaCita() { return fechaCita; }
    public String getMensaje() { return mensaje; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public boolean isLeido() { return leido; }
}
