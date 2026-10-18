package com.fullstack.fullstack.controller;

import com.fullstack.fullstack.dto.EstudianteDtos.EstudianteRequest;
import com.fullstack.fullstack.dto.EstudianteDtos.EstudianteResponse;
import com.fullstack.fullstack.service.EstudianteService;
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
 * /api/estudiantes: GET lo pueden usar ADMIN y DOCENTE; crear, editar y eliminar solo ADMIN
 * (se exige en SecurityConfig).
 */
@RestController
@RequestMapping("/api/estudiantes")
public class EstudianteController {

    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    /** GET /api/estudiantes?q=texto: lista o busca por nombre o documento. */
    @GetMapping
    public List<EstudianteResponse> listar(@RequestParam(required = false) String q) {
        return estudianteService.listar(q);
    }

    @GetMapping("/{id}")
    public EstudianteResponse obtener(@PathVariable Long id) {
        return estudianteService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<EstudianteResponse> crear(@Valid @RequestBody EstudianteRequest request) {
        EstudianteResponse creado = estudianteService.crear(request);
        return ResponseEntity.created(URI.create("/api/estudiantes/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    public EstudianteResponse actualizar(@PathVariable Long id, @Valid @RequestBody EstudianteRequest request) {
        return estudianteService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        estudianteService.eliminar(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
