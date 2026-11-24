package com.g1.trading.config;

import com.g1.trading.portafolio.VentaNoPermitidaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ErrorResponse(
            String timestamp,
            int status,
            String error,
            String mensaje
    ) {}

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> manejarArgumentoInvalido(IllegalArgumentException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler({IllegalStateException.class, VentaNoPermitidaException.class})
    public ResponseEntity<ErrorResponse> manejarConflictoNegocio(RuntimeException ex) {
        return construirRespuesta(HttpStatus.CONFLICT, ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> construirRespuesta(HttpStatus status, String mensaje) {
        ErrorResponse body = new ErrorResponse(
                Instant.now().toString(),
                status.value(),
                status.getReasonPhrase(),
                mensaje != null ? mensaje : "Error de validación"
        );
        return ResponseEntity.status(status).body(body);
    }
}