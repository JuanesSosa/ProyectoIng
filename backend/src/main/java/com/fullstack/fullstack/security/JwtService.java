package com.fullstack.fullstack.security;

import com.fullstack.fullstack.model.entity.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/** Genera los tokens JWT que se entregan al iniciar sesión. */
@Service
public class JwtService {

    /** Token firmado y segundos que dura. */
    public record Token(String valor, long expiraEnSegundos) {
    }

    private final JwtEncoder encoder;
    private final long expiracionMinutos;

    public JwtService(JwtEncoder encoder, @Value("${app.jwt.expiration-minutes}") long expiracionMinutos) {
        this.encoder = encoder;
        this.expiracionMinutos = expiracionMinutos;
    }

    /**
     * Crea el token de un usuario. Claims: subject = correo, uid (id del usuario), nombre, rol y (solo para estudiantes) estudianteId.
     * El estudianteId viaja como texto para evitar diferencias de tipo numérico al decodificarlo.
     */
    public Token generar(Usuario usuario) {
        Instant ahora = Instant.now();
        Instant expira = ahora.plus(expiracionMinutos, ChronoUnit.MINUTES);

        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer("gestor-notas")
                .issuedAt(ahora)
                .expiresAt(expira)
                .subject(usuario.getCorreo())
                .claim("uid", String.valueOf(usuario.getId()))
                .claim("nombre", usuario.getNombre())
                .claim("rol", usuario.getRol().name());
        if (usuario.getEstudiante() != null) {
            claims.claim("estudianteId", String.valueOf(usuario.getEstudiante().getId()));
        }

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String valor = encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
        return new Token(valor, expiracionMinutos * 60);
    }
}
