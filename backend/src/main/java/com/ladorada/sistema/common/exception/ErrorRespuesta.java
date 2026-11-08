package com.ladorada.sistema.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorRespuesta(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<ErrorCampo> errors,
        List<DetalleStockInsuficiente> detallesStock
) {
    public ErrorRespuesta(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path, null, null);
    }

    public ErrorRespuesta(int status, String error, String message, String path, List<ErrorCampo> errors) {
        this(LocalDateTime.now(), status, error, message, path, errors, null);
    }

    public ErrorRespuesta(int status, String error, String message, String path, List<ErrorCampo> errors, List<DetalleStockInsuficiente> detallesStock) {
        this(LocalDateTime.now(), status, error, message, path, errors, detallesStock);
    }
}
