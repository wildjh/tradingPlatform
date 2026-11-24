package com.g1.trading.mercado.infraestructura.entrada.web;

import com.g1.trading.mercado.aplicacion.InstrumentoNoEncontradoException;
import com.g1.trading.mercado.aplicacion.InstrumentoUseCase;
import com.g1.trading.mercado.aplicacion.SimboloYaRegistradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduce los rechazos del núcleo a respuestas HTTP. */
@RestControllerAdvice
public class MercadoExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> argumentoInvalido(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(SimboloYaRegistradoException.class)
    public ResponseEntity<String> simboloDuplicado(SimboloYaRegistradoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(InstrumentoNoEncontradoException.class)
    public ResponseEntity<String> noEncontrado(InstrumentoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
