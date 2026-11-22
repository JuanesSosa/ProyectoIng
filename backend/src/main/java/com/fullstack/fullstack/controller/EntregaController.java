package com.fullstack.fullstack.controller;

import com.fullstack.fullstack.dto.EntregaDtos.EntregaResponse;
import com.fullstack.fullstack.security.Actor;
import com.fullstack.fullstack.service.EntregaService;
import com.fullstack.fullstack.service.EntregaService.Descarga;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Entregas (archivos) de las actividades. Subir y borrar: solo ESTUDIANTE (se exige en SecurityConfig y en el
 * servicio). Listar y descargar: el dueño, el docente del grupo o el administrador.
 */
@RestController
@RequestMapping("/api")
public class EntregaController {

    private final EntregaService entregaService;

    public EntregaController(EntregaService entregaService) {
        this.entregaService = entregaService;
    }

    /** POST /api/actividades/{id}/entregas (multipart, campo "archivo"). */
    @PostMapping("/actividades/{id}/entregas")
    public ResponseEntity<EntregaResponse> subir(@PathVariable Long id,
                                                 @RequestParam("archivo") MultipartFile archivo,
                                                 @AuthenticationPrincipal Jwt jwt) {
        EntregaResponse creada = entregaService.subir(id, archivo, Actor.de(jwt));
        return ResponseEntity.created(URI.create("/api/entregas/" + creada.id())).body(creada);
    }

    /** GET /api/actividades/{id}/entregas?estudianteId=: el estudiante ve las suyas; admin y docente indican el estudiante. */
    @GetMapping("/actividades/{id}/entregas")
    public List<EntregaResponse> listar(@PathVariable Long id,
                                        @RequestParam(required = false) Long estudianteId,
                                        @AuthenticationPrincipal Jwt jwt) {
        return entregaService.listar(id, estudianteId, Actor.de(jwt));
    }

    /** GET /api/entregas/{id}/descargar: siempre como descarga, nunca para mostrarse dentro de la página. */
    @GetMapping("/entregas/{id}/descargar")
    public ResponseEntity<Resource> descargar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Descarga descarga = entregaService.descargar(id, Actor.de(jwt));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(descarga.nombre(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(descarga.ruta()));
    }

    @DeleteMapping("/entregas/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        entregaService.eliminar(id, Actor.de(jwt));
        return ResponseEntity.noContent().build();
    }
}
