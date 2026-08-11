package com.miguel.gamescollection.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.Map;

/*
 * Cuerpo de respuesta uniforme para todos los errores de la API.
 * JsonInclude.NON_NULL evita que salgan campos vacíos en el JSON.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(OffsetDateTime.now(), status, error, message, path, null);
    }

    public static ApiError withFields(int status, String error, String message,
                                      String path, Map<String, String> fieldErrors) {
        return new ApiError(OffsetDateTime.now(), status, error, message, path, fieldErrors);
    }
}
