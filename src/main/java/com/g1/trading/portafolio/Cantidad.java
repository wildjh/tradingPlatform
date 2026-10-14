package com.g1.trading.portafolio;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Paso 2 · Value Object del contexto Portafolio.
 * <p>
 * Unidades de un instrumento. Invariante: nunca es negativa (cero sí es válido, p. ej. "no poseo nada").
 * Misma escala para que 5 y 5.0 sean iguales.
 */
public record Cantidad(BigDecimal valor) implements Comparable<Cantidad> {

    private static final int DECIMALES = 4;

    public static final Cantidad CERO = new Cantidad(BigDecimal.ZERO);

    public Cantidad {
        if (valor == null) {
            throw new IllegalArgumentException("La cantidad necesita un valor");
        }
        if (valor.signum() < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        valor = valor.setScale(DECIMALES, RoundingMode.HALF_UP);
    }

    public static Cantidad de(BigDecimal valor) {
        return new Cantidad(valor);
    }

    public Cantidad sumar(Cantidad otra) {
        return new Cantidad(valor.add(otra.valor));
    }

    public Cantidad restar(Cantidad otra) {
        if (otra.valor.compareTo(valor) > 0) {
            throw new IllegalArgumentException("No se puede restar más de lo que hay");
        }
        return new Cantidad(valor.subtract(otra.valor));
    }

    public boolean esCero() {
        return valor.signum() == 0;
    }

    public boolean esMayorOIgualQue(Cantidad otra) {
        return valor.compareTo(otra.valor) >= 0;
    }

    @Override
    public int compareTo(Cantidad otra) {
        return valor.compareTo(otra.valor);
    }
}
