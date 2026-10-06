package com.g1.trading.mercado;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Paso 3 · Servicio de dominio.
 * <p>
 * HU-08: el administrador simula la actualización periódica de precios
 * para que el mercado se sienta vivo. La regla no cabe dentro de un solo
 * {@link Instrumento}: el mismo factor y la misma marca de tiempo se aplican
 * a todo el catálogo, y o se actualizan todos o no se actualiza ninguno.
 * <p>
 * No es un servicio de Spring. No abre transacciones ni habla con otros contextos.
 */
public final class SimulacionPrecioService {

    private static final int DECIMALES = 4;

    /**
     * Calcula la cotización siguiente de cada instrumento y, solo si todas son válidas,
     * las registra en la raíz correspondiente.
     *
     * @param catalogo         instrumentos del mercado en este instante
     * @param factorVariacion  multiplicador mayor a cero (1.02 es +2 %)
     * @param momento          marca de tiempo común de esta simulación
     */
    public void simular(List<Instrumento> catalogo, BigDecimal factorVariacion, Instant momento) {
        if (catalogo == null || catalogo.isEmpty()) {
            throw new IllegalArgumentException("El catálogo a simular no puede estar vacío");
        }
        if (factorVariacion == null || factorVariacion.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El factor de variación debe ser mayor a cero");
        }
        if (momento == null) {
            throw new IllegalArgumentException("La simulación necesita una marca de tiempo");
        }

        // Primera pasada: validar todo el catálogo antes de tocar un agregado.
        List<PrecioCotizado> nuevasCotizaciones = new ArrayList<>();
        for (Instrumento instrumento : catalogo) {
            if (instrumento == null) {
                throw new IllegalArgumentException("El catálogo no puede contener un instrumento nulo");
            }
            if (!momento.isAfter(instrumento.cotizacionActual().momento())) {
                throw new IllegalArgumentException(
                        "La simulación de " + instrumento.simbolo()
                                + " debe ser posterior a su última cotización");
            }
            BigDecimal nuevoValor = instrumento.cotizacionActual().valor()
                    .multiply(factorVariacion)
                    .setScale(DECIMALES, RoundingMode.HALF_UP);
            nuevasCotizaciones.add(new PrecioCotizado(nuevoValor, momento));
        }

        for (int i = 0; i < catalogo.size(); i++) {
            catalogo.get(i).registrarCotizacion(nuevasCotizaciones.get(i));
        }
    }
}
