package com.fullstack.fullstack.exception;

/**
 * Excepción lanzada cuando se busca un recurso (por ejemplo un producto por id) y no existe.
 * El GlobalExceptionHandler la captura y devuelve una respuesta HTTP 404 Not Found con un JSON de error.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
