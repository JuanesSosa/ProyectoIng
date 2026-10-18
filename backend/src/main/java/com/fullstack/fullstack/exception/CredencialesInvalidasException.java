package com.fullstack.fullstack.exception;

/**
 * Correo o contraseña incorrectos, o usuario desactivado. Se usa el mismo mensaje en todos los casos
 * para no revelar si el correo existe. El GlobalExceptionHandler la convierte en HTTP 401.
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Correo o contraseña incorrectos");
    }
}
