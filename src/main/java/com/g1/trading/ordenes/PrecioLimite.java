package com.g1.trading.ordenes;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value Object del contexto Órdenes.
 *
 * Representa el máximo que se acepta pagar en una compra
 * o el mínimo que se acepta recibir en una venta.
 *
 * Invariante: el valor debe ser mayor que cero.
 */
public record PrecioLimite(BigDecimal valor) {

    private static final int DECIMALES = 4;

    public PrecioLimite {
        if (valor == null) {
            throw new IllegalArgumentException(
                    "El precio límite necesita un valor"
            );
        }

        valor = valor.setScale(DECIMALES, RoundingMode.HALF_UP);

        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El precio límite debe ser mayor a cero"
            );
        }
    }
}