package com.fullstack.fullstack.service;

import com.fullstack.fullstack.dto.NotaDtos.AsignaturaResumen;
import com.fullstack.fullstack.dto.NotaDtos.EvaluacionRequest;
import com.fullstack.fullstack.dto.NotaDtos.NotaResponse;
import com.fullstack.fullstack.dto.NotaDtos.PanelResponse;
import com.fullstack.fullstack.dto.NotaDtos.RegistroNotasRequest;
import com.fullstack.fullstack.dto.NotaDtos.RegistroNotasResponse;
import com.fullstack.fullstack.dto.NotaDtos.ResumenNotasResponse;
import com.fullstack.fullstack.exception.ReglaNegocioException;
import com.fullstack.fullstack.exception.ResourceNotFoundException;
import com.fullstack.fullstack.model.entity.Asignatura;
import com.fullstack.fullstack.model.entity.Estudiante;
import com.fullstack.fullstack.model.entity.Nota;
import com.fullstack.fullstack.repository.AsignaturaRepository;
import com.fullstack.fullstack.repository.EstudianteRepository;
import com.fullstack.fullstack.repository.NotaRepository;
import com.fullstack.fullstack.service.PromedioCalculator.Item;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Registro, modificación y consulta de calificaciones y promedios (RF-07 a RF-11). */
@Service
@Transactional
public class NotaService {

    private final NotaRepository notaRepository;
    private final EstudianteRepository estudianteRepository;
    private final AsignaturaRepository asignaturaRepository;

    public NotaService(NotaRepository notaRepository,
                       EstudianteRepository estudianteRepository,
                       AsignaturaRepository asignaturaRepository) {
        this.notaRepository = notaRepository;
        this.estudianteRepository = estudianteRepository;
        this.asignaturaRepository = asignaturaRepository;
    }

    /** Evaluaciones de un estudiante en una asignatura durante un periodo. */
    @Transactional(readOnly = true)
    public List<NotaResponse> listar(Long estudianteId, Long asignaturaId, String periodo) {
        return notaRepository
                .findByEstudianteIdAndAsignaturaIdAndPeriodoOrderByIdAsc(estudianteId, asignaturaId, periodo)
                .stream()
                .map(NotaResponse::from)
                .toList();
    }

    /**
     * Guarda de una vez todas las evaluaciones de un estudiante en una asignatura y periodo (RF-07, RF-08).
     * - Las evaluaciones que ya existían (mismo nombre, sin distinguir mayúsculas) se actualizan.
     * - Las nuevas se crean y las que ya no vienen en la lista se eliminan.
     * - Reglas: notas entre 0.0 y 5.0 (validado en el DTO), nombres sin repetir, pesos todos definidos o
     *   ninguno, y suma de pesos de máximo 100 %.
     */
    public RegistroNotasResponse registrar(RegistroNotasRequest request) {
        Estudiante estudiante = estudianteRepository.findById(request.estudianteId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Estudiante no encontrado con id " + request.estudianteId()));
        Asignatura asignatura = asignaturaRepository.findById(request.asignaturaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asignatura no encontrada con id " + request.asignaturaId()));
        if (!asignatura.isActiva()) {
            throw new ReglaNegocioException("La asignatura " + asignatura.getNombre() + " está desactivada");
        }
        validarEvaluaciones(request.evaluaciones());

        List<Nota> existentes = notaRepository.findByEstudianteIdAndAsignaturaIdAndPeriodoOrderByIdAsc(
                estudiante.getId(), asignatura.getId(), request.periodo());
        Map<String, Nota> existentesPorNombre = new HashMap<>();
        for (Nota nota : existentes) {
            existentesPorNombre.put(clave(nota.getEvaluacion()), nota);
        }

        List<Nota> aGuardar = new ArrayList<>();
        Set<String> clavesUsadas = new HashSet<>();
        for (EvaluacionRequest evaluacion : request.evaluaciones()) {
            String nombre = evaluacion.evaluacion().trim();
            String clave = clave(nombre);
            Nota nota = existentesPorNombre.get(clave);
            if (nota == null) {
                nota = Nota.builder()
                        .estudiante(estudiante)
                        .asignatura(asignatura)
                        .periodo(request.periodo())
                        .build();
            }
            nota.setEvaluacion(nombre);
            nota.setPeso(evaluacion.peso());
            nota.setValor(evaluacion.valor().setScale(2, RoundingMode.HALF_UP));
            aGuardar.add(nota);
            clavesUsadas.add(clave);
        }

        List<Nota> aEliminar = existentes.stream()
                .filter(nota -> !clavesUsadas.contains(clave(nota.getEvaluacion())))
                .toList();
        notaRepository.deleteAll(aEliminar);
        // Ejecuta los borrados antes de los inserts: así una evaluación renombrada no choca con la restricción de unicidad.
        notaRepository.flush();
        List<Nota> guardadas = notaRepository.saveAll(aGuardar);

        List<Item> items = guardadas.stream().map(n -> new Item(n.getValor(), n.getPeso())).toList();
        return new RegistroNotasResponse(
                estudiante.getId(),
                asignatura.getId(),
                request.periodo(),
                guardadas.stream().map(NotaResponse::from).toList(),
                PromedioCalculator.promedio(items),
                PromedioCalculator.pesoTotal(items));
    }

    /** Pantalla "Consulta de notas": todas las asignaturas del periodo con sus notas, promedios y el promedio general. */
    @Transactional(readOnly = true)
    public ResumenNotasResponse resumen(Long estudianteId, String periodo) {
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado con id " + estudianteId));

        Map<Long, List<Nota>> porAsignatura = new LinkedHashMap<>();
        for (Nota nota : notaRepository.findDelPeriodo(estudianteId, periodo)) {
            porAsignatura.computeIfAbsent(nota.getAsignatura().getId(), id -> new ArrayList<>()).add(nota);
        }

        List<AsignaturaResumen> filas = new ArrayList<>();
        for (List<Nota> grupo : porAsignatura.values()) {
            Asignatura asignatura = grupo.get(0).getAsignatura();
            List<Item> items = grupo.stream().map(n -> new Item(n.getValor(), n.getPeso())).toList();
            filas.add(new AsignaturaResumen(
                    asignatura.getId(),
                    asignatura.getCodigo(),
                    asignatura.getNombre(),
                    grupo.stream().map(NotaResponse::from).toList(),
                    PromedioCalculator.promedio(items),
                    PromedioCalculator.pesoTotal(items)));
        }

        List<BigDecimal> promedios = filas.stream().map(AsignaturaResumen::promedio).toList();
        return new ResumenNotasResponse(
                estudiante.getId(),
                estudiante.getNombre(),
                periodo,
                filas,
                PromedioCalculator.promedioGeneral(promedios));
    }

    /** Periodos con notas registradas, del más reciente al más antiguo. */
    @Transactional(readOnly = true)
    public List<String> periodos() {
        return notaRepository.findPeriodos();
    }

    /** Totales del panel principal. */
    @Transactional(readOnly = true)
    public PanelResponse panel() {
        return new PanelResponse(estudianteRepository.count(), asignaturaRepository.count(), notaRepository.count());
    }

    private void validarEvaluaciones(List<EvaluacionRequest> evaluaciones) {
        Set<String> nombres = new HashSet<>();
        boolean conPeso = false;
        boolean sinPeso = false;
        int totalPesos = 0;
        for (EvaluacionRequest evaluacion : evaluaciones) {
            if (!nombres.add(clave(evaluacion.evaluacion()))) {
                throw new ReglaNegocioException(
                        "La evaluación \"" + evaluacion.evaluacion().trim() + "\" está repetida");
            }
            if (evaluacion.peso() == null) {
                sinPeso = true;
            } else {
                conPeso = true;
                totalPesos += evaluacion.peso();
            }
        }
        if (conPeso && sinPeso) {
            throw new ReglaNegocioException("Define el peso de todas las evaluaciones o de ninguna");
        }
        if (totalPesos > 100) {
            throw new ReglaNegocioException(
                    "La suma de los pesos no puede superar 100 % (actualmente suma " + totalPesos + " %)");
        }
    }

    private String clave(String evaluacion) {
        return evaluacion.trim().toLowerCase(Locale.ROOT);
    }
}
