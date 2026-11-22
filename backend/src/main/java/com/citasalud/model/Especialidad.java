package com.citasalud.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.citasalud.model.tarifa.FabricaTarifas;

/** Especialidad médica con su tarifa de consulta (en pesos colombianos). */
@Document(collection = "especialidades")
public class Especialidad {

    @Id
    private String id;
    @Indexed(unique = true)
    private String nombre;
    private String descripcion;
    private long tarifa;
    private boolean activa = true;

    protected Especialidad() {
    }

    public Especialidad(String nombre, String descripcion, long tarifa) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tarifa = tarifa;
    }

    /**
     * GRASP Experto: la especialidad conoce su tarifa y calcula cuánto paga cada tipo de paciente (RN_01).
     * El cálculo concreto lo hace la estrategia que entrega la fábrica (patrón Strategy).
     */
    public long valorPara(TipoPaciente tipoPaciente) {
        return FabricaTarifas.para(tipoPaciente).calcular(tarifa);
    }

    public void desactivar() {
        this.activa = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public long getTarifa() { return tarifa; }
    public void setTarifa(long tarifa) { this.tarifa = tarifa; }
    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
}
