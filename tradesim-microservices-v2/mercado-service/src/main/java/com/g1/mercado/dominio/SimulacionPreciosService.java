package com.g1.mercado.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class SimulacionPreciosService {
    private static final int DECIMALES = 4;

    public void simular(List<Instrumento> catalogo, BigDecimal factorVariacion,
                        Instant momento, String generadoPor) {
        if (catalogo == null || catalogo.isEmpty()) throw new IllegalArgumentException("El catálogo a simular no puede estar vacío");
        if (factorVariacion == null || factorVariacion.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("El factor de variación debe ser mayor a cero");
        if (momento == null) throw new IllegalArgumentException("La simulación necesita una marca de tiempo");
        if (generadoPor == null || generadoPor.isBlank()) throw new IllegalArgumentException("La simulación debe indicar quién la generó");

        List<PrecioCotizado> nuevasCotizaciones = new ArrayList<>();
        for (Instrumento instrumento : catalogo) {
            PrecioCotizado actual = instrumento.obtenerCotizacionActual().precio();
            if (!momento.isAfter(actual.momento())) {
                throw new IllegalArgumentException("La simulación de " + instrumento.id().valor() + " debe ser posterior a su última cotización");
            }
            BigDecimal nuevoValor = actual.valor().multiply(factorVariacion).setScale(DECIMALES, RoundingMode.HALF_UP);
            nuevasCotizaciones.add(new PrecioCotizado(nuevoValor, momento));
        }

        for (int i = 0; i < catalogo.size(); i++) {
            catalogo.get(i).actualizarPrecio(nuevasCotizaciones.get(i), OrigenCotizacion.SIMULACION, generadoPor);
        }
    }
}
