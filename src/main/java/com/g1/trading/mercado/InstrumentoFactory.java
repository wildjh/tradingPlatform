package com.g1.trading.mercado;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Única puerta para registrar un instrumento en el catálogo.
 * Arma el id, el precio cotizado inicial y la cotización trazable,
 * y entrega una raíz que ya cumple sus invariantes.
 */
public final class InstrumentoFactory {

    public Instrumento registrar(String simbolo, String nombre, TipoInstrumento tipo,
                                 BigDecimal precioInicial, Instant momento, String generadoPor) {
        InstrumentoId id = new InstrumentoId(simbolo);
        String nombreLimpio = exigirNombre(nombre);
        if (tipo == null) {
            throw new IllegalArgumentException("El instrumento necesita un tipo (ACCION o ETF)");
        }
        if (generadoPor == null || generadoPor.isBlank()) {
            throw new IllegalArgumentException("La carga inicial debe indicar quién la generó");
        }
        PrecioCotizado precio = new PrecioCotizado(precioInicial, momento);
        Cotizacion inicial = new Cotizacion(precio, OrigenCotizacion.CARGA_INICIAL, generadoPor);
        return new Instrumento(id, nombreLimpio, tipo, inicial);
    }

    private static String exigirNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El instrumento necesita un nombre");
        }
        return nombre.trim();
    }
}
