package com.fullstack.fullstack.controller;

import com.fullstack.fullstack.dto.AuthDtos.LoginRequest;
import com.fullstack.fullstack.dto.AuthDtos.LoginResponse;
import com.fullstack.fullstack.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** POST /api/auth/login: inicio de sesión (RF-01). Es la única ruta pública de la API. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
