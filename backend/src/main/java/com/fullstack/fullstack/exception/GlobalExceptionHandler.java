package com.fullstack.fullstack.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejador global de excepciones de los controladores REST.
 * Convierte cada excepción en un JSON uniforme: timestamp, status, error, message (y "errors" por campo).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 404: el recurso no existe. */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    /** 409: dato duplicado o recurso en uso. */
    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<Map<String, Object>> handleConflicto(ConflictoException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    /** 400: se incumple una regla de negocio. */
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String, Object>> handleReglaNegocio(ReglaNegocioException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    /** 401: credenciales incorrectas en el login. */
    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<Map<String, Object>> handleCredenciales(CredencialesInvalidasException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), null);
    }

    /** 403: el usuario no puede ver o modificar esa información. */
    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<Map<String, Object>> handleAccesoDenegado(AccesoDenegadoException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), null);
    }

    /** 400: falló la validación (@Valid) del cuerpo. Devuelve los errores por campo en "errors". */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> errors.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        return build(HttpStatus.BAD_REQUEST, "Datos inválidos", errors);
    }

    /** 400: el JSON enviado está mal formado o tiene tipos incorrectos. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es un JSON válido", null);
    }

    /** 400: un parámetro de la URL tiene un formato inválido (por ejemplo un id que no es número). */
    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTipoInvalido(TypeMismatchException ex) {
        return build(HttpStatus.BAD_REQUEST, "Parámetro con formato inválido", null);
    }

    /** 409: la base de datos rechazó el dato (por ejemplo, una restricción de unicidad en una carrera de peticiones). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleIntegridad(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad de datos: {}", ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT, "El dato ya existe o está en uso", null);
    }

    /**
     * Cualquier otro error. Las excepciones propias de Spring MVC (ruta inexistente, método no permitido,
     * parámetro faltante...) ya traen su código HTTP: se respeta. El resto es un 500 que se registra en el log
     * sin exponer detalles internos al cliente.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {
        if (ex instanceof ErrorResponse errorResponse) {
            HttpStatus status = HttpStatus.resolve(errorResponse.getStatusCode().value());
            if (status != null) {
                return build(status, status.getReasonPhrase(), null);
            }
        }
        log.error("Error inesperado", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", null);
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message, Map<String, String> errors) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        if (errors != null) {
            body.put("errors", errors);
        }
        return ResponseEntity.status(status).body(body);
    }
}
