package com.citasalud.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.citasalud.dto.HorarioDto;
import com.citasalud.dto.MedicoRequest;
import com.citasalud.dto.MedicoResponse;
import com.citasalud.exception.AccesoDenegadoException;
import com.citasalud.exception.ConflictoException;
import com.citasalud.exception.RecursoNoEncontradoException;
import com.citasalud.exception.ReglaNegocioException;
import com.citasalud.model.Especialidad;
import com.citasalud.model.Medico;
import com.citasalud.model.Rol;
import com.citasalud.repository.UsuarioRepositorio;
import com.citasalud.security.UsuarioActual;

/** Gestión de médicos y de su horario (HU_004, HU_006, HU_007). */
@Service
public class MedicoServicio {

    private final UsuarioRepositorio usuarios;
    private final EspecialidadServicio especialidadServicio;
    private final PasswordEncoder codificador;

    public MedicoServicio(UsuarioRepositorio usuarios, EspecialidadServicio especialidadServicio,
                          PasswordEncoder codificador) {
        this.usuarios = usuarios;
        this.especialidadServicio = especialidadServicio;
        this.codificador = codificador;
    }

    public List<MedicoResponse> listar(String especialidadId, boolean incluirInactivos) {
        return usuarios.findByRolOrderByNombreAsc(Rol.MEDICO).stream()
                .map(Medico.class::cast)
                .filter(m -> incluirInactivos || m.isActivo())
                .filter(m -> especialidadId == null || especialidadId.isBlank()
                        || especialidadId.equals(m.getEspecialidad().getId()))
                .map(MedicoResponse::desde)
                .toList();
    }

    public MedicoResponse obtener(String id) {
        return MedicoResponse.desde(buscarMedico(id));
    }

    public MedicoResponse crear(MedicoRequest datos) {
        String correo = AuthServicio.normalizarCorreo(datos.correo());
        if (usuarios.existsByCorreo(correo)) {
            throw new ConflictoException("El correo " + correo + " ya está en uso");
        }
        if (datos.contrasena() == null || datos.contrasena().length() < 8) {
            throw new ReglaNegocioException("La contraseña inicial debe tener al menos 8 caracteres");
        }
        Especialidad especialidad = especialidadActiva(datos.especialidadId());
        Medico medico = new Medico(datos.nombre().trim(), correo, codificador.encode(datos.contrasena()),
                datos.telefono().trim(), datos.registroMedico().trim(), especialidad, datos.consultorio().trim());
        return MedicoResponse.desde(usuarios.save(medico));
    }

    public MedicoResponse actualizar(String id, MedicoRequest datos) {
        Medico medico = buscarMedico(id);
        String correo = AuthServicio.normalizarCorreo(datos.correo());
        if (!medico.getCorreo().equals(correo) && usuarios.existsByCorreo(correo)) {
            throw new ConflictoException("El correo " + correo + " ya está en uso");
        }
        medico.setNombre(datos.nombre().trim());
        medico.setCorreo(correo);
        medico.setTelefono(datos.telefono().trim());
        medico.setRegistroMedico(datos.registroMedico().trim());
        medico.setConsultorio(datos.consultorio().trim());
        medico.setEspecialidad(especialidadActiva(datos.especialidadId()));
        if (datos.contrasena() != null && !datos.contrasena().isBlank()) {
            medico.setContrasenaHash(codificador.encode(datos.contrasena()));
        }
        return MedicoResponse.desde(usuarios.save(medico));
    }

    public void desactivar(String id) {
        Medico medico = buscarMedico(id);
        medico.desactivar();
        usuarios.save(medico);
    }

    /** RF_006: el médico solo cambia su propio horario; el administrador puede cambiar cualquiera. */
    public MedicoResponse actualizarHorarios(String id, List<HorarioDto> horarios, UsuarioActual actual) {
        if (actual.es(Rol.MEDICO) && !actual.id().equals(id)) {
            throw new AccesoDenegadoException("Solo puedes cambiar tu propio horario");
        }
        Medico medico = buscarMedico(id);
        medico.definirHorarios(horarios.stream().map(HorarioDto::aModelo).toList());
        return MedicoResponse.desde(usuarios.save(medico));
    }

    Medico buscarMedico(String id) {
        return usuarios.findById(id)
                .filter(Medico.class::isInstance)
                .map(Medico.class::cast)
                .orElseThrow(() -> new RecursoNoEncontradoException("El médico no existe"));
    }

    private Especialidad especialidadActiva(String especialidadId) {
        Especialidad especialidad = especialidadServicio.buscar(especialidadId);
        if (!especialidad.isActiva()) {
            throw new ReglaNegocioException("La especialidad " + especialidad.getNombre() + " está desactivada");
        }
        return especialidad;
    }
}
