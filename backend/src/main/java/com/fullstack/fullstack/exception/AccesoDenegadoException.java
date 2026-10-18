package com.fullstack.fullstack.exception;

/**
 * El usuario autenticado intenta acceder a información que no le corresponde
 * (por ejemplo, un estudiante pidiendo las notas de otro). Se convierte en HTTP 403 Forbidden.
 */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String message) {
        super(message);
    }
}
