package com.fullstack.fullstack.controller;

import com.fullstack.fullstack.dto.CorreoDtos.EnvioResponse;
import com.fullstack.fullstack.security.Actor;
import com.fullstack.fullstack.service.CorreoNotasService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Envío de notas por correo (solo el docente del grupo; ver SecurityConfig y CorreoNotasService). */
@RestController
@RequestMapping("/api")
public class CorreoController {

    private final CorreoNotasService correoNotasService;

    public CorreoController(CorreoNotasService correoNotasService) {
        this.correoNotasService = correoNotasService;
    }

    /** POST /api/grupos/{id}/correos[?estudianteId=]: sin estudianteId se envía a todo el grupo. */
    @PostMapping("/grupos/{id}/correos")
    public EnvioResponse enviar(@PathVariable Long id,
                                @RequestParam(required = false) Long estudianteId,
                                @AuthenticationPrincipal Jwt jwt) {
        return correoNotasService.enviar(id, estudianteId, Actor.de(jwt));
    }
}
