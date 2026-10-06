package com.g1.trading.mercado;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Paso 5 · Factory del agregado Instrumento.
 * <p>
 * Un solo lugar para el evento "instrumento registrado en el catálogo".
 * Normaliza el símbolo, arma el {@link PrecioCotizado} inicial y entrega
 * una raíz que ya cumple sus invariantes.
 */
public final class InstrumentoFactory {

    public Instrumento registrar(String simbolo, String nombre, TipoInstrumento tipo,
                                 BigDecimal precioInicial, Instant momento) {
        String simboloLimpio = exigirSimbolo(simbolo);
        String nombreLimpio = exigirNombre(nombre);
        if (tipo == null) {
            throw new IllegalArgumentException("El instrumento necesita un tipo (ACCION o ETF)");
        }
        PrecioCotizado cotizacionInicial = new PrecioCotizado(precioInicial, momento);
        return new Instrumento(simboloLimpio, nombreLimpio, tipo, cotizacionInicial);
    }

    private static String exigirSimbolo(String simbolo) {
        if (simbolo == null || simbolo.isBlank()) {
            throw new IllegalArgumentException("El símbolo es obligatorio");
        }
        String limpio = simbolo.trim().toUpperCase();
        if (!limpio.matches("[A-Z]{1,5}")) {
            throw new IllegalArgumentException("El símbolo debe tener de 1 a 5 letras");
        }
        return limpio;
    }

    private static String exigirNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El instrumento necesita un nombre");
        }
        return nombre.trim();
    }
}
