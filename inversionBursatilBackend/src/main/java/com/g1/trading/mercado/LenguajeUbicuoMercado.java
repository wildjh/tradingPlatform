package com.g1.trading.mercado;

import java.util.Map;

/**
 * Lenguaje ubicuo del contexto Mercado.
 * Cada término significa una sola cosa aquí. No se usan palabras de órdenes
 * ni de portafolio.
 */
public final class LenguajeUbicuoMercado {

    private LenguajeUbicuoMercado() {
    }

    public static Map<String, String> terminos() {
        return Map.of(
                "Instrumento",
                "Activo del catálogo (acción o ETF) que se consulta con su precio actual.",
                "InstrumentoId",
                "Identidad del instrumento. Otros contextos guardan solo el String valor, nunca el objeto Instrumento.",
                "TipoInstrumento",
                "Clase del activo en el catálogo: ACCION o ETF.",
                "Cotizacion",
                "Precio vigente de un instrumento, con qué lo generó y quién lo generó.",
                "PrecioCotizado",
                "Valor de una cotización: siempre mayor a cero y con marca de tiempo.",
                "HistoricoPrecios",
                "Serie de cotizaciones de un mismo instrumento, ordenada en el tiempo.",
                "SimulacionPrecios",
                "Actualización periódica de todo el catálogo para que el mercado se sienta vivo (HU-08)."
        );
    }
}
