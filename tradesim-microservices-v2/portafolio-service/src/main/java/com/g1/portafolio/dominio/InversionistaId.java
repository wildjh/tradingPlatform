package com.g1.portafolio.dominio;

public record InversionistaId(String valor) {
    public InversionistaId {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("InversionistaId no puede ser vacío");
        }
    }
}
