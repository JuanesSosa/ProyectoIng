package com.fullstack.fullstack.service;

import com.fullstack.fullstack.exception.AccesoDenegadoException;
import com.fullstack.fullstack.exception.ReglaNegocioException;
import com.fullstack.fullstack.model.entity.Grupo;
import com.fullstack.fullstack.model.entity.Rol;
import com.fullstack.fullstack.repository.MatriculaRepository;
import com.fullstack.fullstack.security.Actor;
import org.springframework.stereotype.Component;

/**
 * Reglas de acceso por grupo (además de los roles que exige SecurityConfig por ruta):
 * - ADMIN ve todos los grupos; DOCENTE solo los suyos; ESTUDIANTE solo aquellos en los que está matriculado.
 * - Solo el docente a cargo del grupo define actividades y califica.
 * - Un ESTUDIANTE solo consulta sus propias notas.
 */
@Component
public class Permisos {

    private final MatriculaRepository matriculaRepository;

    public Permisos(MatriculaRepository matriculaRepository) {
        this.matriculaRepository = matriculaRepository;
    }

    /** Ver el grupo y sus actividades: admin, docente del grupo o estudiante matriculado. */
    public void puedeVerGrupo(Grupo grupo, Actor actor) {
        boolean permitido = switch (actor.rol()) {
            case ADMIN -> true;
            case DOCENTE -> esDocenteDelGrupo(grupo, actor);
            case ESTUDIANTE -> actor.estudianteId() != null
                    && matriculaRepository.existsByGrupoIdAndEstudianteId(grupo.getId(), actor.estudianteId());
        };
        if (!permitido) {
            throw new AccesoDenegadoException("No tienes acceso a este grupo");
        }
    }

    /** Ver datos de todos los estudiantes del grupo: admin o docente del grupo. */
    public void puedeVerDetalleGrupo(Grupo grupo, Actor actor) {
        if (actor.rol() == Rol.ADMIN) {
            return;
        }
        if (actor.rol() == Rol.DOCENTE && esDocenteDelGrupo(grupo, actor)) {
            return;
        }
        throw new AccesoDenegadoException("No tienes acceso a los estudiantes de este grupo");
    }

    /** Definir actividades, rúbricas y calificar: solo el docente a cargo. */
    public void puedeCalificar(Grupo grupo, Actor actor) {
        if (actor.rol() != Rol.DOCENTE || !esDocenteDelGrupo(grupo, actor)) {
            throw new AccesoDenegadoException("Solo el docente a cargo del grupo puede hacer esto");
        }
    }

    /**
     * De qué estudiante se consultan las notas. ESTUDIANTE: siempre el suyo (403 si pide otro).
     * ADMIN/DOCENTE: el indicado.
     */
    public Long estudianteConsultado(Actor actor, Long solicitado) {
        if (actor.rol() == Rol.ESTUDIANTE) {
            if (actor.estudianteId() == null) {
                throw new AccesoDenegadoException("Tu usuario no está asociado a ningún estudiante");
            }
            if (solicitado != null && !solicitado.equals(actor.estudianteId())) {
                throw new AccesoDenegadoException("Solo puedes consultar tus propias notas");
            }
            return actor.estudianteId();
        }
        if (solicitado == null) {
            throw new ReglaNegocioException("Debes indicar el estudiante");
        }
        return solicitado;
    }

    private boolean esDocenteDelGrupo(Grupo grupo, Actor actor) {
        return actor.usuarioId() != null && actor.usuarioId().equals(grupo.getDocente().getId());
    }
}
