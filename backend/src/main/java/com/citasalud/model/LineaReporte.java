package com.citasalud.model;

/** Una fila del reporte: los totales de una especialidad. */
public class LineaReporte {

    private final String especialidad;
    private int programadas;
    private int atendidas;
    private int canceladas;
    private int noAsistio;
    private long recaudado;

    public LineaReporte(String especialidad) {
        this.especialidad = especialidad;
    }

    /** Suma una cita a los contadores de esta línea. */
    public void registrar(Cita cita) {
        switch (cita.getEstado()) {
            case PROGRAMADA -> programadas++;
            case ATENDIDA -> atendidas++;
            case CANCELADA -> canceladas++;
            case NO_ASISTIO -> noAsistio++;
        }
        recaudado += cita.valorRecaudado();
    }

    public int getTotal() {
        return programadas + atendidas + canceladas + noAsistio;
    }

    public String getEspecialidad() { return especialidad; }
    public int getProgramadas() { return programadas; }
    public int getAtendidas() { return atendidas; }
    public int getCanceladas() { return canceladas; }
    public int getNoAsistio() { return noAsistio; }
    public long getRecaudado() { return recaudado; }
}
