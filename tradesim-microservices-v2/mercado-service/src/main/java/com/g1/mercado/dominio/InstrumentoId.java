package com.g1.mercado.dominio;

public record InstrumentoId(String valor) {
    public InstrumentoId {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El símbolo es obligatorio");
        }
        valor = valor.trim().toUpperCase();
        if (!valor.matches("[A-Z]{1,5}")) {
            throw new IllegalArgumentException("El símbolo debe tener de 1 a 5 letras");
        }
    }
}
