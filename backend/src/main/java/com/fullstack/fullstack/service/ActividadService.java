package com.fullstack.fullstack.service;

import com.fullstack.fullstack.dto.ActividadDtos.ActividadRequest;
import com.fullstack.fullstack.dto.ActividadDtos.ActividadResponse;
import com.fullstack.fullstack.dto.ActividadDtos.CriterioRequest;
import com.fullstack.fullstack.dto.ActividadDtos.RubricaRequest;
import com.fullstack.fullstack.exception.ConflictoException;
import com.fullstack.fullstack.exception.ReglaNegocioException;
import com.fullstack.fullstack.exception.ResourceNotFoundException;
import com.fullstack.fullstack.model.entity.Actividad;
import com.fullstack.fullstack.model.entity.Criterio;
import com.fullstack.fullstack.model.entity.Grupo;
import com.fullstack.fullstack.repository.ActividadRepository;
import com.fullstack.fullstack.repository.CalificacionRepository;
import com.fullstack.fullstack.repository.CriterioRepository;
import com.fullstack.fullstack.repository.GrupoRepository;
import com.fullstack.fullstack.security.Actor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Actividades de un grupo (Parcial 1, Talleres, Proyecto...) y la rúbrica de cada una.
 * Solo el docente a cargo del grupo las define; los pesos de las actividades suman máximo 100
 * y los de los criterios de una rúbrica también.
 */
@Service
@Transactional
public class ActividadService {

    private final ActividadRepository actividadRepository;
    private final CriterioRepository criterioRepository;
    private final CalificacionRepository calificacionRepository;
    private final GrupoRepository grupoRepository;
    private final Permisos permisos;

    public ActividadService(ActividadRepository actividadRepository,
                            CriterioRepository criterioRepository,
                            CalificacionRepository calificacionRepository,
                            GrupoRepository grupoRepository,
                            Permisos permisos) {
        this.actividadRepository = actividadRepository;
        this.criterioRepository = criterioRepository;
        this.calificacionRepository = calificacionRepository;
        this.grupoRepository = grupoRepository;
        this.permisos = permisos;
    }

    @Transactional(readOnly = true)
    public List<ActividadResponse> listar(Long grupoId, Actor actor) {
        Grupo grupo = buscarGrupo(grupoId);
        permisos.puedeVerGrupo(grupo, actor);
        Map<Long, List<Criterio>> criteriosPorActividad = new HashMap<>();
        for (Criterio criterio : criterioRepository.findDelGrupo(grupoId)) {
            criteriosPorActividad.computeIfAbsent(criterio.getActividad().getId(), k -> new ArrayList<>()).add(criterio);
        }
        return actividadRepository.findByGrupoIdOrderByIdAsc(grupoId).stream()
                .map(a -> ActividadResponse.from(a, criteriosPorActividad.getOrDefault(a.getId(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ActividadResponse obtener(Long id, Actor actor) {
        Actividad actividad = buscar(id);
        permisos.puedeVerGrupo(actividad.getGrupo(), actor);
        return ActividadResponse.from(actividad, criterioRepository.findByActividadIdOrderByIdAsc(id));
    }

    public ActividadResponse crear(Long grupoId, ActividadRequest request, Actor actor) {
        Grupo grupo = buscarGrupo(grupoId);
        permisos.puedeCalificar(grupo, actor);
        validarActividad(grupoId, null, request);
        Actividad actividad = Actividad.builder()
                .grupo(grupo)
                .nombre(request.nombre().trim())
                .peso(request.peso())
                .build();
        return ActividadResponse.from(actividadRepository.save(actividad), List.of());
    }

    public ActividadResponse actualizar(Long id, ActividadRequest request, Actor actor) {
        Actividad actividad = buscar(id);
        permisos.puedeCalificar(actividad.getGrupo(), actor);
        validarActividad(actividad.getGrupo().getId(), id, request);
        actividad.setNombre(request.nombre().trim());
        actividad.setPeso(request.peso());
        actividadRepository.save(actividad);
        return ActividadResponse.from(actividad, criterioRepository.findByActividadIdOrderByIdAsc(id));
    }

    /** Elimina la actividad con su rúbrica; no se permite si ya hay calificaciones, para no perder notas. */
    public void eliminar(Long id, Actor actor) {
        Actividad actividad = buscar(id);
        permisos.puedeCalificar(actividad.getGrupo(), actor);
        if (calificacionRepository.existeEnActividad(id)) {
            throw new ConflictoException("No se puede eliminar la actividad porque ya tiene calificaciones");
        }
        criterioRepository.deleteAll(criterioRepository.findByActividadIdOrderByIdAsc(id));
        actividadRepository.delete(actividad);
    }

    /**
     * Guarda la rúbrica completa. Los criterios con id se actualizan, los nuevos se crean y los que ya
     * no vienen se eliminan (si alguno tiene calificaciones, se rechaza para no perder notas).
     */
    public ActividadResponse guardarRubrica(Long id, RubricaRequest request, Actor actor) {
        Actividad actividad = buscar(id);
        permisos.puedeCalificar(actividad.getGrupo(), actor);
        validarRubrica(request.criterios());

        Map<Long, Criterio> existentes = new HashMap<>();
        for (Criterio criterio : criterioRepository.findByActividadIdOrderByIdAsc(id)) {
            existentes.put(criterio.getId(), criterio);
        }

        Set<Long> conservados = new HashSet<>();
        List<Criterio> aGuardar = new ArrayList<>();
        for (CriterioRequest item : request.criterios()) {
            Criterio criterio;
            if (item.id() == null) {
                criterio = Criterio.builder().actividad(actividad).build();
            } else {
                criterio = existentes.get(item.id());
                if (criterio == null) {
                    throw new ReglaNegocioException("El criterio " + item.id() + " no pertenece a esta actividad");
                }
                conservados.add(item.id());
            }
            criterio.setNombre(item.nombre().trim());
            criterio.setPeso(item.peso());
            aGuardar.add(criterio);
        }

        List<Criterio> aEliminar = existentes.values().stream()
                .filter(c -> !conservados.contains(c.getId()))
                .toList();
        for (Criterio criterio : aEliminar) {
            if (calificacionRepository.existeEnCriterio(criterio.getId())) {
                throw new ConflictoException("No se puede quitar el criterio \"" + criterio.getNombre()
                        + "\" porque ya tiene calificaciones");
            }
        }
        criterioRepository.deleteAll(aEliminar);
        criterioRepository.saveAll(aGuardar);
        return ActividadResponse.from(actividad, criterioRepository.findByActividadIdOrderByIdAsc(id));
    }

    // ---------- validaciones ----------

    /** Nombre único dentro del grupo (sin distinguir mayúsculas) y pesos de actividades que sumen máximo 100. */
    private void validarActividad(Long grupoId, Long idActual, ActividadRequest request) {
        String nombre = request.nombre().trim();
        int otrosPesos = 0;
        for (Actividad otra : actividadRepository.findByGrupoIdOrderByIdAsc(grupoId)) {
            if (otra.getId().equals(idActual)) {
                continue;
            }
            if (otra.getNombre().trim().equalsIgnoreCase(nombre)) {
                throw new ConflictoException("Ya existe una actividad llamada \"" + nombre + "\" en este grupo");
            }
            otrosPesos += otra.getPeso();
        }
        if (otrosPesos + request.peso() > 100) {
            throw new ReglaNegocioException("Los porcentajes de las actividades no pueden sumar más de 100 (disponible: "
                    + (100 - otrosPesos) + ")");
        }
    }

    private void validarRubrica(List<CriterioRequest> criterios) {
        Set<String> nombres = new HashSet<>();
        Set<Long> ids = new HashSet<>();
        int total = 0;
        for (CriterioRequest criterio : criterios) {
            if (!nombres.add(criterio.nombre().trim().toLowerCase(Locale.ROOT))) {
                throw new ReglaNegocioException("El criterio \"" + criterio.nombre().trim() + "\" está repetido");
            }
            if (criterio.id() != null && !ids.add(criterio.id())) {
                throw new ReglaNegocioException("El criterio " + criterio.id() + " viene repetido");
            }
            total += criterio.peso();
        }
        if (total > 100) {
            throw new ReglaNegocioException("Los porcentajes de la rúbrica no pueden sumar más de 100 (suman " + total + ")");
        }
    }

    private Actividad buscar(Long id) {
        return actividadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada con id " + id));
    }

    private Grupo buscarGrupo(Long id) {
        return grupoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado con id " + id));
    }
}
