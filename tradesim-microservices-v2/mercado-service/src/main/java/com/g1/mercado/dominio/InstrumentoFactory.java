package com.g1.mercado.dominio;

import java.math.BigDecimal;
import java.time.Instant;

public final class InstrumentoFactory {
    public Instrumento registrar(String simbolo, String nombre, TipoInstrumento tipo,
                                 BigDecimal precioInicial, Instant momento, String generadoPor) {
        InstrumentoId id = new InstrumentoId(simbolo);
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El instrumento necesita un nombre");
        if (tipo == null) throw new IllegalArgumentException("El instrumento necesita un tipo (ACCION o ETF)");
        if (generadoPor == null || generadoPor.isBlank()) throw new IllegalArgumentException("La carga inicial debe indicar quién la generó");
        PrecioCotizado precio = new PrecioCotizado(precioInicial, momento);
        Cotizacion inicial = new Cotizacion(precio, OrigenCotizacion.CARGA_INICIAL, generadoPor);
        return new Instrumento(id, nombre.trim(), tipo, inicial);
    }
}
