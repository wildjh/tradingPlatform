package com.g1.mercado.dominio;

public record Cotizacion(PrecioCotizado precio, OrigenCotizacion origen, String generadoPor) {
    public Cotizacion {
        if (precio == null) throw new IllegalArgumentException("La cotización necesita un precio cotizado");
        if (origen == null) throw new IllegalArgumentException("La cotización debe indicar qué la generó");
        if (generadoPor == null || generadoPor.isBlank()) throw new IllegalArgumentException("La cotización debe indicar quién la generó");
        generadoPor = generadoPor.trim();
    }
}
