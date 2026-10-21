package com.fullstack.fullstack.security;

import com.fullstack.fullstack.model.entity.Rol;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Quién hace la petición, tomado del token JWT: id de usuario, rol y (si es estudiante) su estudianteId.
 * Los servicios lo usan para decidir qué grupos y notas puede ver o modificar cada persona.
 */
public record Actor(Long usuarioId, Rol rol, Long estudianteId) {

    public static Actor de(Jwt jwt) {
        return new Actor(numero(jwt.getClaimAsString("uid")), Rol.valueOf(jwt.getClaimAsString("rol")),
                numero(jwt.getClaimAsString("estudianteId")));
    }

    private static Long numero(String texto) {
        return texto == null ? null : Long.valueOf(texto);
    }
}
