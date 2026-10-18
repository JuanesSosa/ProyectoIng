package com.fullstack.fullstack.exception;

/**
 * Se intenta crear o borrar algo que choca con datos existentes (documento o código repetido,
 * estudiante con notas, etc.). El GlobalExceptionHandler la convierte en HTTP 409 Conflict.
 */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String message) {
        super(message);
    }
}
