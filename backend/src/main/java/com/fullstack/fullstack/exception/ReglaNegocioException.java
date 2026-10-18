package com.fullstack.fullstack.exception;

/**
 * Se incumple una regla de negocio (por ejemplo, los pesos suman más de 100 %).
 * El GlobalExceptionHandler la convierte en HTTP 400 Bad Request.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String message) {
        super(message);
    }
}
