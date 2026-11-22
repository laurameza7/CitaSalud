package com.citasalud.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.citasalud.dto.EspecialidadRequest;
import com.citasalud.dto.EspecialidadResponse;
import com.citasalud.dto.ValorCitaResponse;
import com.citasalud.exception.ConflictoException;
import com.citasalud.exception.RecursoNoEncontradoException;
import com.citasalud.model.Especialidad;
import com.citasalud.model.TipoPaciente;
import com.citasalud.model.tarifa.EstrategiaTarifa;
import com.citasalud.model.tarifa.FabricaTarifas;
import com.citasalud.repository.EspecialidadRepositorio;

/** Gestión y consulta de especialidades (HU_003, HU_007) y cálculo del valor de la cita (RF_010). */
@Service
public class EspecialidadServicio {

    private final EspecialidadRepositorio especialidades;

    public EspecialidadServicio(EspecialidadRepositorio especialidades) {
        this.especialidades = especialidades;
    }

    public List<EspecialidadResponse> listar(boolean incluirInactivas) {
        List<Especialidad> lista = incluirInactivas
                ? especialidades.findAllByOrderByNombreAsc()
                : especialidades.findByActivaTrueOrderByNombreAsc();
        return lista.stream().map(EspecialidadResponse::desde).toList();
    }

    public EspecialidadResponse obtener(String id) {
        return EspecialidadResponse.desde(buscar(id));
    }

    public EspecialidadResponse crear(EspecialidadRequest datos) {
        String nombre = datos.nombre().trim();
        if (especialidades.existsByNombreIgnoreCase(nombre)) {
            throw new ConflictoException("Ya existe la especialidad " + nombre);
        }
        return EspecialidadResponse.desde(especialidades.save(new Especialidad(nombre, datos.descripcion(), datos.tarifa())));
    }

    public EspecialidadResponse actualizar(String id, EspecialidadRequest datos) {
        Especialidad especialidad = buscar(id);
        String nombre = datos.nombre().trim();
        if (!especialidad.getNombre().equalsIgnoreCase(nombre) && especialidades.existsByNombreIgnoreCase(nombre)) {
            throw new ConflictoException("Ya existe la especialidad " + nombre);
        }
        especialidad.setNombre(nombre);
        especialidad.setDescripcion(datos.descripcion());
        especialidad.setTarifa(datos.tarifa());
        return EspecialidadResponse.desde(especialidades.save(especialidad));
    }

    public void desactivar(String id) {
        Especialidad especialidad = buscar(id);
        especialidad.desactivar();
        especialidades.save(especialidad);
    }

    public ValorCitaResponse valorPara(String id, TipoPaciente tipo) {
        Especialidad especialidad = buscar(id);
        EstrategiaTarifa estrategia = FabricaTarifas.para(tipo);
        long total = especialidad.valorPara(tipo);
        long descuento = especialidad.getTarifa() + EstrategiaTarifa.CARGO_ADMINISTRATIVO - total;
        return new ValorCitaResponse(especialidad.getNombre(), tipo, especialidad.getTarifa(),
                (int) Math.round(estrategia.descuento() * 100), descuento, EstrategiaTarifa.CARGO_ADMINISTRATIVO, total);
    }

    Especialidad buscar(String id) {
        return especialidades.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La especialidad no existe"));
    }
}
