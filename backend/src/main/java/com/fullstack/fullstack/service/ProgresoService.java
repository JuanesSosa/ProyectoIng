package com.fullstack.fullstack.service;

import com.fullstack.fullstack.dto.ActividadDtos.ActividadResumen;
import com.fullstack.fullstack.dto.ActividadDtos.CalificacionRequest;
import com.fullstack.fullstack.dto.ActividadDtos.CalificacionesRequest;
import com.fullstack.fullstack.dto.ActividadDtos.CriterioNota;
import com.fullstack.fullstack.dto.ActividadDtos.EstudianteActividad;
import com.fullstack.fullstack.dto.ActividadDtos.ResumenGrupoResponse;
import com.fullstack.fullstack.dto.ActividadDtos.RubricaEstudianteResponse;
import com.fullstack.fullstack.exception.ReglaNegocioException;
import com.fullstack.fullstack.exception.ResourceNotFoundException;
import com.fullstack.fullstack.model.entity.Actividad;
import com.fullstack.fullstack.model.entity.Calificacion;
import com.fullstack.fullstack.model.entity.Criterio;
import com.fullstack.fullstack.model.entity.Estudiante;
import com.fullstack.fullstack.model.entity.Grupo;
import com.fullstack.fullstack.model.entity.Matricula;
import com.fullstack.fullstack.repository.ActividadRepository;
import com.fullstack.fullstack.repository.CalificacionRepository;
import com.fullstack.fullstack.repository.CriterioRepository;
import com.fullstack.fullstack.repository.GrupoRepository;
import com.fullstack.fullstack.repository.MatriculaRepository;
import com.fullstack.fullstack.security.Actor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Calificaciones por rúbrica y seguimiento del progreso de cada estudiante hacia su nota final.
 * El docente califica criterio por criterio; el sistema calcula la nota de cada actividad y la del grupo.
 */
@Service
@Transactional
public class ProgresoService {

    private final ActividadRepository actividadRepository;
    private final CriterioRepository criterioRepository;
    private final CalificacionRepository calificacionRepository;
    private final MatriculaRepository matriculaRepository;
    private final GrupoRepository grupoRepository;
    private final Permisos permisos;

    public ProgresoService(ActividadRepository actividadRepository,
                           CriterioRepository criterioRepository,
                           CalificacionRepository calificacionRepository,
                           MatriculaRepository matriculaRepository,
                           GrupoRepository grupoRepository,
                           Permisos permisos) {
        this.actividadRepository = actividadRepository;
        this.criterioRepository = criterioRepository;
        this.calificacionRepository = calificacionRepository;
        this.matriculaRepository = matriculaRepository;
        this.grupoRepository = grupoRepository;
        this.permisos = permisos;
    }

    // ---------- por actividad ----------

    /** Estudiantes del grupo con su nota y avance en la actividad (para "Seleccionar estudiante"). */
    @Transactional(readOnly = true)
    public List<EstudianteActividad> estudiantesDeActividad(Long actividadId, Actor actor) {
        Actividad actividad = buscarActividad(actividadId);
        Grupo grupo = actividad.getGrupo();
        permisos.puedeVerDetalleGrupo(grupo, actor);

        List<Criterio> criterios = criterioRepository.findByActividadIdOrderByIdAsc(actividadId);
        Map<Long, Map<Long, BigDecimal>> notasPorEstudiante = new HashMap<>();
        for (Calificacion c : calificacionRepository.findDeActividad(actividadId)) {
            notasPorEstudiante.computeIfAbsent(c.getEstudiante().getId(), k -> new HashMap<>())
                    .put(c.getCriterio().getId(), c.getValor());
        }

        List<EstudianteActividad> resultado = new ArrayList<>();
        for (Matricula matricula : matriculaRepository.findByGrupoIdOrderByEstudianteNombreAsc(grupo.getId())) {
            Estudiante e = matricula.getEstudiante();
            var res = CalculoNotas.actividad(aCriterios(criterios, notasPorEstudiante.getOrDefault(e.getId(), Map.of())));
            resultado.add(new EstudianteActividad(e.getId(), e.getDocumento(), e.getNombre(), res.nota(), res.avance()));
        }
        return resultado;
    }

    /** Rúbrica de la actividad con las notas de un estudiante. */
    @Transactional(readOnly = true)
    public RubricaEstudianteResponse rubrica(Long actividadId, Long estudianteSolicitado, Actor actor) {
        Actividad actividad = buscarActividad(actividadId);
        permisos.puedeVerGrupo(actividad.getGrupo(), actor);
        Long estudianteId = permisos.estudianteConsultado(actor, estudianteSolicitado);
        Estudiante estudiante = estudianteMatriculado(actividad.getGrupo(), estudianteId);
        return construirRubrica(actividad, estudiante);
    }

    /**
     * Guarda las calificaciones de un estudiante en criterios de la actividad (solo el docente del grupo).
     * Un valor null quita la nota de ese criterio.
     */
    public RubricaEstudianteResponse calificar(Long actividadId, Long estudianteId, CalificacionesRequest request,
                                               Actor actor) {
        Actividad actividad = buscarActividad(actividadId);
        permisos.puedeCalificar(actividad.getGrupo(), actor);
        Estudiante estudiante = estudianteMatriculado(actividad.getGrupo(), estudianteId);

        Map<Long, Criterio> criterios = new HashMap<>();
        for (Criterio criterio : criterioRepository.findByActividadIdOrderByIdAsc(actividadId)) {
            criterios.put(criterio.getId(), criterio);
        }
        Map<Long, Calificacion> existentes = new HashMap<>();
        for (Calificacion c : calificacionRepository.findDeEstudianteEnActividad(estudianteId, actividadId)) {
            existentes.put(c.getCriterio().getId(), c);
        }

        Set<Long> vistos = new HashSet<>();
        List<Calificacion> aGuardar = new ArrayList<>();
        List<Calificacion> aEliminar = new ArrayList<>();
        for (CalificacionRequest item : request.calificaciones()) {
            Criterio criterio = criterios.get(item.criterioId());
            if (criterio == null) {
                throw new ReglaNegocioException("El criterio " + item.criterioId() + " no pertenece a esta actividad");
            }
            if (!vistos.add(item.criterioId())) {
                throw new ReglaNegocioException("El criterio \"" + criterio.getNombre() + "\" viene repetido");
            }
            Calificacion actual = existentes.get(item.criterioId());
            if (item.valor() == null) {
                if (actual != null) {
                    aEliminar.add(actual);
                }
                continue;
            }
            BigDecimal valor = item.valor().setScale(2, RoundingMode.HALF_UP);
            if (actual == null) {
                actual = Calificacion.builder().criterio(criterio).estudiante(estudiante).build();
            }
            actual.setValor(valor);
            aGuardar.add(actual);
        }
        calificacionRepository.deleteAll(aEliminar);
        calificacionRepository.saveAll(aGuardar);
        return construirRubrica(actividad, estudiante);
    }

    // ---------- por grupo ----------

    /** Seguimiento de un estudiante en el grupo: nota por actividad, acumulado, promedio y avance. */
    @Transactional(readOnly = true)
    public ResumenGrupoResponse resumen(Long grupoId, Long estudianteSolicitado, Actor actor) {
        Grupo grupo = buscarGrupo(grupoId);
        permisos.puedeVerGrupo(grupo, actor);
        Long estudianteId = permisos.estudianteConsultado(actor, estudianteSolicitado);
        Estudiante estudiante = estudianteMatriculado(grupo, estudianteId);

        List<Actividad> actividades = actividadRepository.findByGrupoIdOrderByIdAsc(grupoId);
        Map<Long, List<Criterio>> criterios = criteriosPorActividad(grupoId);
        Map<Long, BigDecimal> notas = new HashMap<>();
        for (Calificacion c : calificacionRepository.findDeGrupo(grupoId)) {
            if (c.getEstudiante().getId().equals(estudianteId)) {
                notas.put(c.getCriterio().getId(), c.getValor());
            }
        }
        return construirResumen(grupo, estudiante, actividades, criterios, notas);
    }

    /** Seguimiento de todos los estudiantes del grupo (admin o docente del grupo). */
    @Transactional(readOnly = true)
    public List<ResumenGrupoResponse> resumenDelGrupo(Long grupoId, Actor actor) {
        Grupo grupo = buscarGrupo(grupoId);
        permisos.puedeVerDetalleGrupo(grupo, actor);

        List<Actividad> actividades = actividadRepository.findByGrupoIdOrderByIdAsc(grupoId);
        Map<Long, List<Criterio>> criterios = criteriosPorActividad(grupoId);
        Map<Long, Map<Long, BigDecimal>> notasPorEstudiante = new HashMap<>();
        for (Calificacion c : calificacionRepository.findDeGrupo(grupoId)) {
            notasPorEstudiante.computeIfAbsent(c.getEstudiante().getId(), k -> new HashMap<>())
                    .put(c.getCriterio().getId(), c.getValor());
        }

        List<ResumenGrupoResponse> resultado = new ArrayList<>();
        for (Matricula matricula : matriculaRepository.findByGrupoIdOrderByEstudianteNombreAsc(grupoId)) {
            Estudiante e = matricula.getEstudiante();
            resultado.add(construirResumen(grupo, e, actividades, criterios,
                    notasPorEstudiante.getOrDefault(e.getId(), Map.of())));
        }
        return resultado;
    }

    // ---------- construcción de respuestas ----------

    private RubricaEstudianteResponse construirRubrica(Actividad actividad, Estudiante estudiante) {
        List<Criterio> criterios = criterioRepository.findByActividadIdOrderByIdAsc(actividad.getId());
        Map<Long, BigDecimal> notas = new HashMap<>();
        for (Calificacion c : calificacionRepository.findDeEstudianteEnActividad(estudiante.getId(), actividad.getId())) {
            notas.put(c.getCriterio().getId(), c.getValor());
        }
        var resultado = CalculoNotas.actividad(aCriterios(criterios, notas));
        return new RubricaEstudianteResponse(actividad.getId(), actividad.getNombre(), actividad.getPeso(),
                estudiante.getId(), estudiante.getNombre(), aCriterioNotas(criterios, notas),
                resultado.nota(), resultado.avance());
    }

    private ResumenGrupoResponse construirResumen(Grupo grupo, Estudiante estudiante, List<Actividad> actividades,
                                                  Map<Long, List<Criterio>> criteriosPorActividad,
                                                  Map<Long, BigDecimal> notas) {
        List<ActividadResumen> resumenes = new ArrayList<>();
        List<CalculoNotas.Actividad> paraCalculo = new ArrayList<>();
        int pesoActividades = 0;
        for (Actividad actividad : actividades) {
            List<Criterio> criterios = criteriosPorActividad.getOrDefault(actividad.getId(), List.of());
            List<CalculoNotas.Criterio> calculo = aCriterios(criterios, notas);
            var resultado = CalculoNotas.actividad(calculo);
            resumenes.add(new ActividadResumen(actividad.getId(), actividad.getNombre(), actividad.getPeso(),
                    aCriterioNotas(criterios, notas), resultado.nota(), resultado.avance()));
            paraCalculo.add(new CalculoNotas.Actividad(actividad.getPeso(), calculo));
            pesoActividades += actividad.getPeso();
        }
        var total = CalculoNotas.grupo(paraCalculo);
        return new ResumenGrupoResponse(grupo.getId(), grupo.getAsignatura().getId(), grupo.getAsignatura().getNombre(),
                grupo.getNombre(), grupo.getPeriodo(), estudiante.getId(), estudiante.getDocumento(),
                estudiante.getNombre(), resumenes, pesoActividades, total.promedio(), total.acumulado(), total.avance());
    }

    private Map<Long, List<Criterio>> criteriosPorActividad(Long grupoId) {
        Map<Long, List<Criterio>> mapa = new HashMap<>();
        for (Criterio criterio : criterioRepository.findDelGrupo(grupoId)) {
            mapa.computeIfAbsent(criterio.getActividad().getId(), k -> new ArrayList<>()).add(criterio);
        }
        return mapa;
    }

    private List<CalculoNotas.Criterio> aCriterios(List<Criterio> criterios, Map<Long, BigDecimal> notas) {
        return criterios.stream().map(c -> new CalculoNotas.Criterio(notas.get(c.getId()), c.getPeso())).toList();
    }

    private List<CriterioNota> aCriterioNotas(List<Criterio> criterios, Map<Long, BigDecimal> notas) {
        return criterios.stream()
                .map(c -> new CriterioNota(c.getId(), c.getNombre(), c.getPeso(), notas.get(c.getId())))
                .toList();
    }

    /** El estudiante debe estar matriculado en el grupo; si no, se rechaza. */
    private Estudiante estudianteMatriculado(Grupo grupo, Long estudianteId) {
        return matriculaRepository.findByGrupoIdAndEstudianteId(grupo.getId(), estudianteId)
                .map(Matricula::getEstudiante)
                .orElseThrow(() -> new ReglaNegocioException("El estudiante no está matriculado en este grupo"));
    }

    private Actividad buscarActividad(Long id) {
        return actividadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada con id " + id));
    }

    private Grupo buscarGrupo(Long id) {
        return grupoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado con id " + id));
    }
}
