package com.g1.trading.compartido;

/** Identificador compartido entre contextos. Los contextos se referencian solo por id. */
public record InversionistaId(String valor) {
    public InversionistaId {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("InversionistaId no puede ser vacío");
        }
    }
}
