package com.fullstack.fullstack.controller;

import com.fullstack.fullstack.dto.AsignaturaDtos.AsignaturaRequest;
import com.fullstack.fullstack.dto.AsignaturaDtos.AsignaturaResponse;
import com.fullstack.fullstack.service.AsignaturaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
 * /api/asignaturas: GET lo pueden usar ADMIN y DOCENTE; crear, editar y desactivar solo ADMIN
 * (se exige en SecurityConfig).
 */
@RestController
@RequestMapping("/api/asignaturas")
public class AsignaturaController {

    private final AsignaturaService asignaturaService;

    public AsignaturaController(AsignaturaService asignaturaService) {
        this.asignaturaService = asignaturaService;
    }

    /** GET /api/asignaturas?soloActivas=true: con soloActivas=true omite las desactivadas. */
    @GetMapping
    public List<AsignaturaResponse> listar(@RequestParam(defaultValue = "false") boolean soloActivas) {
        return asignaturaService.listar(soloActivas);
    }

    @GetMapping("/{id}")
    public AsignaturaResponse obtener(@PathVariable Long id) {
        return asignaturaService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<AsignaturaResponse> crear(@Valid @RequestBody AsignaturaRequest request) {
        AsignaturaResponse creada = asignaturaService.crear(request);
        return ResponseEntity.created(URI.create("/api/asignaturas/" + creada.id())).body(creada);
    }

    @PutMapping("/{id}")
    public AsignaturaResponse actualizar(@PathVariable Long id, @Valid @RequestBody AsignaturaRequest request) {
        return asignaturaService.actualizar(id, request);
    }

    /** Desactiva la asignatura (no la borra: las notas históricas se conservan). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        asignaturaService.desactivar(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
