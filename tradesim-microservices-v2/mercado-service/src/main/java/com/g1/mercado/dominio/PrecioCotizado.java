package com.g1.mercado.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

public record PrecioCotizado(BigDecimal valor, Instant momento) {
    private static final int DECIMALES = 4;

    public PrecioCotizado {
        if (valor == null) throw new IllegalArgumentException("El precio cotizado necesita un valor");
        if (momento == null) throw new IllegalArgumentException("El precio cotizado necesita una marca de tiempo");
        valor = valor.setScale(DECIMALES, RoundingMode.HALF_UP);
        if (valor.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("El precio cotizado debe ser mayor a cero");
    }
}
