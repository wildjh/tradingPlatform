package com.g1.trading.mercado;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * Paso 2 · Value Object.
 * <p>
 * Un precio cotizado no tiene identidad propia: dos cotizaciones con el mismo
 * valor y la misma marca de tiempo son la misma cotización. Por eso es un
 * {@code record} y no una entidad.
 * <p>
 * Invariante: el valor es mayor a cero y siempre hay marca de tiempo.
 * Así el catálogo no puede publicar un precio inválido (el mercado simulado
 * sigue siendo consultable y coherente).
 */
public record PrecioCotizado(BigDecimal valor, Instant momento) {

    private static final int DECIMALES = 4;

    public PrecioCotizado {
        if (valor == null) {
            throw new IllegalArgumentException("El precio cotizado necesita un valor");
        }
        if (momento == null) {
            throw new IllegalArgumentException("El precio cotizado necesita una marca de tiempo");
        }
        // Misma escala para que 10 y 10.0 se comparen igual.
        valor = valor.setScale(DECIMALES, RoundingMode.HALF_UP);
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio cotizado debe ser mayor a cero");
        }
    }
}
