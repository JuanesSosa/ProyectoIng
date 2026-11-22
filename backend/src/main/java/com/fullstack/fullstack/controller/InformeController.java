package com.fullstack.fullstack.controller;

import com.fullstack.fullstack.dto.InformeDtos.EnvioInformeResponse;
import com.fullstack.fullstack.security.Actor;
import com.fullstack.fullstack.service.InformeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** POST /api/grupos/{id}/informe/enviar: el docente envía el informe de notas por correo. */
@RestController
@RequestMapping("/api")
public class InformeController {

    private final InformeService informeService;

    public InformeController(InformeService informeService) {
        this.informeService = informeService;
    }

    @PostMapping("/grupos/{id}/informe/enviar")
    public EnvioInformeResponse enviar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return informeService.enviarInformeGrupo(id, Actor.de(jwt));
    }
}