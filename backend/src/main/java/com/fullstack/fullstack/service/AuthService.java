package com.fullstack.fullstack.service;

import com.fullstack.fullstack.dto.AuthDtos.LoginRequest;
import com.fullstack.fullstack.dto.AuthDtos.LoginResponse;
import com.fullstack.fullstack.exception.CredencialesInvalidasException;
import com.fullstack.fullstack.model.entity.Usuario;
import com.fullstack.fullstack.repository.UsuarioRepository;
import com.fullstack.fullstack.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Inicio de sesión (RF-01). El cierre de sesión (RF-12) lo hace el frontend descartando el token. */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Valida correo y contraseña. Si el usuario no existe, está desactivado o la contraseña no coincide
     * se lanza el mismo error, para no revelar cuál de los datos falló.
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(request.correo().trim())
                .orElseThrow(CredencialesInvalidasException::new);

        if (!usuario.isActivo() || !passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new CredencialesInvalidasException();
        }

        JwtService.Token token = jwtService.generar(usuario);
        Long estudianteId = usuario.getEstudiante() != null ? usuario.getEstudiante().getId() : null;
        return new LoginResponse(token.valor(), "Bearer", token.expiraEnSegundos(),
                usuario.getNombre(), usuario.getCorreo(), usuario.getRol(), estudianteId);
    }
}
