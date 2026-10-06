package com.g1.trading.mercado;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de dominio de la simulación periódica de precios (HU-08).
 * <p>
 * El mismo factor y la misma marca de tiempo se aplican a todo el catálogo:
 * la regla no pertenece a un solo Instrumento. Si una cotización nueva no es
 * válida, no se actualiza ninguna. No conoce órdenes ni portafolio, y no usa Spring.
 */
public final class SimulacionPreciosService {

    private static final int DECIMALES = 4;

    /**
     * @param catalogo        instrumentos a actualizar
     * @param factorVariacion multiplicador mayor a cero (1.02 es +2 %)
     * @param momento         marca de tiempo común de esta simulación
     * @param generadoPor     quién dispara la simulación, como id de texto
     */
    public void simular(List<Instrumento> catalogo, BigDecimal factorVariacion,
                        Instant momento, String generadoPor) {
        if (catalogo == null || catalogo.isEmpty()) {
            throw new IllegalArgumentException("El catálogo a simular no puede estar vacío");
        }
        if (factorVariacion == null || factorVariacion.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El factor de variación debe ser mayor a cero");
        }
        if (momento == null) {
            throw new IllegalArgumentException("La simulación necesita una marca de tiempo");
        }
        if (generadoPor == null || generadoPor.isBlank()) {
            throw new IllegalArgumentException("La simulación debe indicar quién la generó");
        }

        List<PrecioCotizado> nuevasCotizaciones = new ArrayList<>();
        for (Instrumento instrumento : catalogo) {
            if (instrumento == null) {
                throw new IllegalArgumentException("El catálogo no puede contener un instrumento nulo");
            }
            PrecioCotizado actual = instrumento.obtenerCotizacionActual().precio();
            if (!momento.isAfter(actual.momento())) {
                throw new IllegalArgumentException(
                        "La simulación de " + instrumento.id().valor()
                                + " debe ser posterior a su última cotización");
            }
            BigDecimal nuevoValor = actual.valor()
                    .multiply(factorVariacion)
                    .setScale(DECIMALES, RoundingMode.HALF_UP);
            nuevasCotizaciones.add(new PrecioCotizado(nuevoValor, momento));
        }

        for (int i = 0; i < catalogo.size(); i++) {
            catalogo.get(i).actualizarPrecio(
                    nuevasCotizaciones.get(i), OrigenCotizacion.SIMULACION, generadoPor);
        }
    }
}
