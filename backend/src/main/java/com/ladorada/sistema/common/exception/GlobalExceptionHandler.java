package com.ladorada.sistema.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErrorCampo> campos = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            campos.add(new ErrorCampo(fieldError.getField(), fieldError.getDefaultMessage()));
        }

        ErrorRespuesta error = new ErrorRespuesta(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Error de validación en los datos enviados",
                request.getRequestURI(),
                campos
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorRespuesta> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        List<ErrorCampo> campos = ex.getConstraintViolations().stream()
                .map(cv -> new ErrorCampo(cv.getPropertyPath().toString(), cv.getMessage()))
                .toList();

        ErrorRespuesta error = new ErrorRespuesta(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Restricción de validación violada",
                request.getRequestURI(),
                campos
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorRespuesta> handleBadRequest(Exception ex, HttpServletRequest request) {
        String mensaje = ex.getMessage() != null ? ex.getMessage() : "Solicitud inválida";
        if (ex instanceof HttpMessageNotReadableException) {
            mensaje = "El cuerpo de la solicitud no tiene un formato JSON válido o contiene datos erróneos";
        }
        ErrorRespuesta error = new ErrorRespuesta(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                mensaje,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler({
            RecursoNoEncontradoException.class,
            NoResourceFoundException.class
    })
    public ResponseEntity<ErrorRespuesta> handleNotFound(Exception ex, HttpServletRequest request) {
        ErrorRespuesta error = new ErrorRespuesta(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(CodigoDuplicadoException.class)
    public ResponseEntity<ErrorRespuesta> handleCodigoDuplicado(CodigoDuplicadoException ex, HttpServletRequest request) {
        ErrorRespuesta error = new ErrorRespuesta(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<ErrorRespuesta> handleStockInsuficiente(StockInsuficienteException ex, HttpServletRequest request) {
        ErrorRespuesta error = new ErrorRespuesta(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                null,
                ex.getDetalles()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorRespuesta> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Violación de integridad de datos: {}", ex.getMessage());
        ErrorRespuesta error = new ErrorRespuesta(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                "Conflicto de integridad de datos en el sistema",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Error inesperado no controlado: ", ex);
        ErrorRespuesta error = new ErrorRespuesta(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Ocurrió un error interno en el servidor",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
