package com.fullstack.fullstack.controller;

import com.fullstack.fullstack.dto.ActividadDtos.ActividadRequest;
import com.fullstack.fullstack.dto.ActividadDtos.ActividadResponse;
import com.fullstack.fullstack.dto.ActividadDtos.ResumenGrupoResponse;
import com.fullstack.fullstack.dto.GrupoDtos.GrupoRequest;
import com.fullstack.fullstack.dto.GrupoDtos.GrupoResponse;
import com.fullstack.fullstack.dto.GrupoDtos.MatriculadoResponse;
import com.fullstack.fullstack.dto.GrupoDtos.PanelResponse;
import com.fullstack.fullstack.security.Actor;
import com.fullstack.fullstack.service.ActividadService;
import com.fullstack.fullstack.service.GrupoService;
import com.fullstack.fullstack.service.ProgresoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * /api/grupos: grupos, matrículas, actividades del grupo y seguimiento.
 * Los roles por ruta se exigen en SecurityConfig; qué grupos ve cada persona lo decide Permisos.
 */
@RestController
@RequestMapping("/api")
public class GrupoController {

    private final GrupoService grupoService;
    private final ActividadService actividadService;
    private final ProgresoService progresoService;

    public GrupoController(GrupoService grupoService, ActividadService actividadService,
                           ProgresoService progresoService) {
        this.grupoService = grupoService;
        this.actividadService = actividadService;
        this.progresoService = progresoService;
    }

    /** GET /api/grupos?asignaturaId=: grupos que la persona puede ver según su rol. */
    @GetMapping("/grupos")
    public List<GrupoResponse> listar(@RequestParam(required = false) Long asignaturaId,
                                      @AuthenticationPrincipal Jwt jwt) {
        return grupoService.listar(Actor.de(jwt), asignaturaId);
    }

    @GetMapping("/grupos/{id}")
    public GrupoResponse obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return grupoService.obtener(id, Actor.de(jwt));
    }

    @PostMapping("/grupos")
    public ResponseEntity<GrupoResponse> crear(@Valid @RequestBody GrupoRequest request) {
        GrupoResponse creado = grupoService.crear(request);
        return ResponseEntity.created(URI.create("/api/grupos/" + creado.id())).body(creado);
    }

    @PutMapping("/grupos/{id}")
    public GrupoResponse actualizar(@PathVariable Long id, @Valid @RequestBody GrupoRequest request) {
        return grupoService.actualizar(id, request);
    }

    @DeleteMapping("/grupos/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        grupoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- matrículas ----------

    @GetMapping("/grupos/{id}/estudiantes")
    public List<MatriculadoResponse> estudiantes(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return grupoService.estudiantes(id, Actor.de(jwt));
    }

    @PostMapping("/grupos/{id}/estudiantes/{estudianteId}")
    public ResponseEntity<MatriculadoResponse> matricular(@PathVariable Long id, @PathVariable Long estudianteId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(grupoService.matricular(id, estudianteId));
    }

    @DeleteMapping("/grupos/{id}/estudiantes/{estudianteId}")
    public ResponseEntity<Void> desmatricular(@PathVariable Long id, @PathVariable Long estudianteId) {
        grupoService.desmatricular(id, estudianteId);
        return ResponseEntity.noContent().build();
    }

    // ---------- actividades y seguimiento ----------

    @GetMapping("/grupos/{id}/actividades")
    public List<ActividadResponse> actividades(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return actividadService.listar(id, Actor.de(jwt));
    }

    @PostMapping("/grupos/{id}/actividades")
    public ResponseEntity<ActividadResponse> crearActividad(@PathVariable Long id,
                                                            @Valid @RequestBody ActividadRequest request,
                                                            @AuthenticationPrincipal Jwt jwt) {
        ActividadResponse creada = actividadService.crear(id, request, Actor.de(jwt));
        return ResponseEntity.created(URI.create("/api/actividades/" + creada.id())).body(creada);
    }

    /** GET /api/grupos/{id}/resumen?estudianteId=: seguimiento de un estudiante (el estudiante ve solo el suyo). */
    @GetMapping("/grupos/{id}/resumen")
    public ResumenGrupoResponse resumen(@PathVariable Long id,
                                        @RequestParam(required = false) Long estudianteId,
                                        @AuthenticationPrincipal Jwt jwt) {
        return progresoService.resumen(id, estudianteId, Actor.de(jwt));
    }

    /** GET /api/grupos/{id}/progreso: seguimiento de todos los estudiantes del grupo (admin o docente). */
    @GetMapping("/grupos/{id}/progreso")
    public List<ResumenGrupoResponse> progreso(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return progresoService.resumenDelGrupo(id, Actor.de(jwt));
    }

    /** GET /api/panel: totales del panel principal (ADMIN y DOCENTE). */
    @GetMapping("/panel")
    public PanelResponse panel() {
        return grupoService.panel();
    }
}
