package com.g1.trading.mercado;

/**
 * Cotización de un instrumento: el precio, qué la generó y quién la generó.
 * El cuándo vive en {@link PrecioCotizado#momento()}.
 * {@code generadoPor} es un id en texto (por ejemplo el administrador de mercado),
 * nunca un objeto de otro subdominio.
 */
public record Cotizacion(PrecioCotizado precio, OrigenCotizacion origen, String generadoPor) {

    public Cotizacion {
        if (precio == null) {
            throw new IllegalArgumentException("La cotización necesita un precio cotizado");
        }
        if (origen == null) {
            throw new IllegalArgumentException("La cotización debe indicar qué la generó");
        }
        if (generadoPor == null || generadoPor.isBlank()) {
            throw new IllegalArgumentException("La cotización debe indicar quién la generó");
        }
        generadoPor = generadoPor.trim();
    }
}
