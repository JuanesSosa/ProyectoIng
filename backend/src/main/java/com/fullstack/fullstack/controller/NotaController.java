package com.fullstack.fullstack.controller;

import com.fullstack.fullstack.dto.NotaDtos.NotaResponse;
import com.fullstack.fullstack.dto.NotaDtos.PanelResponse;
import com.fullstack.fullstack.dto.NotaDtos.RegistroNotasRequest;
import com.fullstack.fullstack.dto.NotaDtos.RegistroNotasResponse;
import com.fullstack.fullstack.dto.NotaDtos.ResumenNotasResponse;
import com.fullstack.fullstack.exception.AccesoDenegadoException;
import com.fullstack.fullstack.exception.ReglaNegocioException;
import com.fullstack.fullstack.service.NotaService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Notas y promedios.
 * - PUT /api/notas/registro: solo DOCENTE (se exige en SecurityConfig).
 * - Las consultas las puede hacer cualquier usuario autenticado, pero un ESTUDIANTE solo ve sus propias notas:
 *   aquí se fuerza su estudianteId y se rechaza (403) si pide el de otro.
 */
@RestController
@RequestMapping("/api")
public class NotaController {

    private final NotaService notaService;

    public NotaController(NotaService notaService) {
        this.notaService = notaService;
    }

    /** GET /api/notas?estudianteId=&asignaturaId=&periodo=: evaluaciones de un estudiante en una asignatura y periodo. */
    @GetMapping("/notas")
    public List<NotaResponse> listar(@RequestParam(required = false) Long estudianteId,
                                     @RequestParam Long asignaturaId,
                                     @RequestParam String periodo,
                                     @AuthenticationPrincipal Jwt jwt) {
        return notaService.listar(resolverEstudiante(jwt, estudianteId), asignaturaId, periodo);
    }

    /** GET /api/notas/resumen?estudianteId=&periodo=: pantalla "Consulta de notas" con promedios. */
    @GetMapping("/notas/resumen")
    public ResumenNotasResponse resumen(@RequestParam(required = false) Long estudianteId,
                                        @RequestParam String periodo,
                                        @AuthenticationPrincipal Jwt jwt) {
        return notaService.resumen(resolverEstudiante(jwt, estudianteId), periodo);
    }

    /** GET /api/notas/periodos: periodos con notas registradas. */
    @GetMapping("/notas/periodos")
    public List<String> periodos() {
        return notaService.periodos();
    }

    /** PUT /api/notas/registro: guarda todas las evaluaciones de un estudiante en una asignatura y periodo. */
    @PutMapping("/notas/registro")
    public RegistroNotasResponse registrar(@Valid @RequestBody RegistroNotasRequest request) {
        return notaService.registrar(request);
    }

    /** GET /api/panel: totales del panel principal (ADMIN y DOCENTE). */
    @GetMapping("/panel")
    public PanelResponse panel() {
        return notaService.panel();
    }

    /**
     * Decide de qué estudiante se consultan las notas.
     * ESTUDIANTE: siempre el suyo (del token); si pide otro id, 403. ADMIN/DOCENTE: el que indiquen.
     */
    private Long resolverEstudiante(Jwt jwt, Long solicitado) {
        if ("ESTUDIANTE".equals(jwt.getClaimAsString("rol"))) {
            String propio = jwt.getClaimAsString("estudianteId");
            if (propio == null) {
                throw new AccesoDenegadoException("Tu usuario no está asociado a ningún estudiante");
            }
            Long propioId = Long.valueOf(propio);
            if (solicitado != null && !solicitado.equals(propioId)) {
                throw new AccesoDenegadoException("Solo puedes consultar tus propias notas");
            }
            return propioId;
        }
        if (solicitado == null) {
            throw new ReglaNegocioException("Debes indicar el estudiante");
        }
        return solicitado;
    }
}
