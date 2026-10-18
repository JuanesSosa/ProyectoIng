package com.fullstack.fullstack.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Configuración de los tokens JWT. Se firman con HMAC-SHA256 usando una clave secreta (app.jwt.secret).
 * El mismo secreto sirve para firmar (al hacer login) y para verificar (en cada petición).
 */
@Configuration
public class JwtConfig {

    private static SecretKey clave(String secreto) {
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException(
                    "app.jwt.secret debe tener al menos 32 caracteres (defínelo con la variable de entorno JWT_SECRET)");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    /** Firma los tokens al iniciar sesión. */
    @Bean
    public JwtEncoder jwtEncoder(@Value("${app.jwt.secret}") String secreto) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(clave(secreto)));
    }

    /** Verifica la firma y la expiración de los tokens que llegan en la cabecera Authorization. */
    @Bean
    public JwtDecoder jwtDecoder(@Value("${app.jwt.secret}") String secreto) {
        return NimbusJwtDecoder.withSecretKey(clave(secreto)).macAlgorithm(MacAlgorithm.HS256).build();
    }
}
