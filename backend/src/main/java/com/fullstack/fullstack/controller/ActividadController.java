package com.fullstack.fullstack.controller;

import com.fullstack.fullstack.dto.ActividadDtos.ActividadRequest;
import com.fullstack.fullstack.dto.ActividadDtos.ActividadResponse;
import com.fullstack.fullstack.dto.ActividadDtos.CalificacionesRequest;
import com.fullstack.fullstack.dto.ActividadDtos.EstudianteActividad;
import com.fullstack.fullstack.dto.ActividadDtos.RubricaEstudianteResponse;
import com.fullstack.fullstack.dto.ActividadDtos.RubricaRequest;
import com.fullstack.fullstack.security.Actor;
import com.fullstack.fullstack.service.ActividadService;
import com.fullstack.fullstack.service.ProgresoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * /api/actividades: editar actividades, rúbricas y calificar (solo el DOCENTE del grupo, se exige en
 * SecurityConfig y en Permisos). Consultar: admin, docente del grupo y, solo lo propio, el estudiante.
 */
@RestController
@RequestMapping("/api/actividades")
public class ActividadController {

    private final ActividadService actividadService;
    private final ProgresoService progresoService;

    public ActividadController(ActividadService actividadService, ProgresoService progresoService) {
        this.actividadService = actividadService;
        this.progresoService = progresoService;
    }

    @GetMapping("/{id}")
    public ActividadResponse obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return actividadService.obtener(id, Actor.de(jwt));
    }

    @PutMapping("/{id}")
    public ActividadResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActividadRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        return actividadService.actualizar(id, request, Actor.de(jwt));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        actividadService.eliminar(id, Actor.de(jwt));
        return ResponseEntity.noContent().build();
    }

    /** PUT /api/actividades/{id}/rubrica: define los criterios de la actividad (reemplaza la rúbrica). */
    @PutMapping("/{id}/rubrica")
    public ActividadResponse guardarRubrica(@PathVariable Long id, @Valid @RequestBody RubricaRequest request,
                                            @AuthenticationPrincipal Jwt jwt) {
        return actividadService.guardarRubrica(id, request, Actor.de(jwt));
    }

    /** GET /api/actividades/{id}/estudiantes: estudiantes del grupo con su nota y avance en la actividad. */
    @GetMapping("/{id}/estudiantes")
    public List<EstudianteActividad> estudiantes(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return progresoService.estudiantesDeActividad(id, Actor.de(jwt));
    }

    /** GET /api/actividades/{id}/rubrica?estudianteId=: rúbrica con las notas de un estudiante. */
    @GetMapping("/{id}/rubrica")
    public RubricaEstudianteResponse rubrica(@PathVariable Long id,
                                             @RequestParam(required = false) Long estudianteId,
                                             @AuthenticationPrincipal Jwt jwt) {
        return progresoService.rubrica(id, estudianteId, Actor.de(jwt));
    }

    /** PUT /api/actividades/{id}/estudiantes/{estudianteId}/calificaciones: guarda las notas por criterio. */
    @PutMapping("/{id}/estudiantes/{estudianteId}/calificaciones")
    public RubricaEstudianteResponse calificar(@PathVariable Long id, @PathVariable Long estudianteId,
                                               @Valid @RequestBody CalificacionesRequest request,
                                               @AuthenticationPrincipal Jwt jwt) {
        return progresoService.calificar(id, estudianteId, request, Actor.de(jwt));
    }
}
