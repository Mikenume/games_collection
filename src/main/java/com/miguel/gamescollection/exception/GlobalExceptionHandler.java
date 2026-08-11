package com.miguel.gamescollection.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/*
 * Centraliza el tratamiento de errores de toda la API.
 * Sin esto, cualquier excepción acabaría en la Whitelabel Error Page
 * o en un 500 genérico sin información útil para el frontend.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex,
                                                   HttpServletRequest request) {
        ApiError body = ApiError.of(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /*
     * Se dispara cuando falla una anotación de validación (@NotBlank, @Min...)
     * sobre un parámetro marcado con @Valid en el controller.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex,
                                                     HttpServletRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage())
        );

        ApiError body = ApiError.withFields(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Hay errores de validación en los datos enviados",
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.badRequest().body(body);
    }

    /*
     * Salta cuando la base de datos rechaza la operación: UNIQUE duplicado,
     * CHECK incumplido, o FK con ON DELETE RESTRICT.
     * 409 Conflict es el código correcto: la petición es válida pero
     * choca con el estado actual de los datos.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleIntegrity(DataIntegrityViolationException ex,
                                                    HttpServletRequest request) {
        ApiError body = ApiError.of(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                "La operación viola una restricción de la base de datos "
                        + "(valor duplicado, referencia en uso o dato fuera de rango)",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex,
                                                          HttpServletRequest request) {
        ApiError body = ApiError.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(body);
    }
}
