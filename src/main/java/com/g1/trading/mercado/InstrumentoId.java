package com.g1.trading.mercado;

/**
 * Identidad del Instrumento dentro del catálogo.
 * El valor es el símbolo (por ejemplo AAPL). Otros subdominios guardan ese
 * String y no este objeto ni el Instrumento completo.
 */
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
