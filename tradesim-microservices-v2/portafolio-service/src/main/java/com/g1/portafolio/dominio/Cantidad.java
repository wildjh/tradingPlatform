package com.g1.portafolio.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Cantidad(BigDecimal valor) implements Comparable<Cantidad> {
    private static final int DECIMALES = 4;
    public static final Cantidad CERO = new Cantidad(BigDecimal.ZERO);

    public Cantidad {
        if (valor == null) throw new IllegalArgumentException("La cantidad necesita un valor");
        if (valor.signum() < 0) throw new IllegalArgumentException("La cantidad no puede ser negativa");
        valor = valor.setScale(DECIMALES, RoundingMode.HALF_UP);
    }

    public static Cantidad de(BigDecimal valor) { return new Cantidad(valor); }
    public Cantidad sumar(Cantidad otra) { return new Cantidad(valor.add(otra.valor)); }
    public Cantidad restar(Cantidad otra) {
        if (otra.valor.compareTo(valor) > 0) throw new IllegalArgumentException("No se puede restar más de lo que hay");
        return new Cantidad(valor.subtract(otra.valor));
    }
    public boolean esCero() { return valor.signum() == 0; }
    public boolean esMayorOIgualQue(Cantidad otra) { return valor.compareTo(otra.valor) >= 0; }

    @Override
    public int compareTo(Cantidad otra) { return valor.compareTo(otra.valor); }
}
