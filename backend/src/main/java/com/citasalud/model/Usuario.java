package com.citasalud.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Clase abstracta para todos los usuarios con cuenta.
 * Paciente, Medico, Recepcionista y Administrador heredan de ella y se guardan
 * en la misma colección "usuarios" (MongoDB guarda el tipo en el campo _class).
 */
@Document(collection = "usuarios")
public abstract class Usuario {

    @Id
    private String id;
    private String nombre;
    @Indexed(unique = true)
    private String correo;
    private String contrasenaHash;
    private String telefono;
    private Rol rol;
    private boolean activo = true;

    protected Usuario() {
    }

    protected Usuario(String nombre, String correo, String contrasenaHash, String telefono, Rol rol) {
        this.nombre = nombre;
        this.correo = correo;
        this.contrasenaHash = contrasenaHash;
        this.telefono = telefono;
        this.rol = rol;
    }

    /** GRASP Experto: el usuario conoce su contraseña cifrada y sabe verificarla. */
    public boolean verificarContrasena(String contrasenaPlana, PasswordEncoder codificador) {
        return contrasenaPlana != null && codificador.matches(contrasenaPlana, contrasenaHash);
    }

    public void desactivar() {
        this.activo = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getContrasenaHash() { return contrasenaHash; }
    public void setContrasenaHash(String contrasenaHash) { this.contrasenaHash = contrasenaHash; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
