package com.citasalud.model;

import java.time.LocalDate;

import org.springframework.data.mongodb.core.mapping.Document;

/** Usuario que agenda citas. Su tipo define el descuento que recibe. */
@Document(collection = "usuarios")
public class Paciente extends Usuario {

    private String documento;
    private LocalDate fechaNacimiento;
    private TipoPaciente tipoPaciente;

    protected Paciente() {
    }

    public Paciente(String nombre, String correo, String contrasenaHash, String telefono,
                    String documento, LocalDate fechaNacimiento, TipoPaciente tipoPaciente) {
        super(nombre, correo, contrasenaHash, telefono, Rol.PACIENTE);
        this.documento = documento;
        this.fechaNacimiento = fechaNacimiento;
        this.tipoPaciente = tipoPaciente;
    }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public TipoPaciente getTipoPaciente() { return tipoPaciente; }
    public void setTipoPaciente(TipoPaciente tipoPaciente) { this.tipoPaciente = tipoPaciente; }
}
