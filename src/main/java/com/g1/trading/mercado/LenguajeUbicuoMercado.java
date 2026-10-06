package com.g1.trading.mercado;

import java.util.Map;

/**
 * Paso 1 · Lenguaje Ubicuo del contexto Mercado.
 * <p>
 * Cada término significa una sola cosa aquí. Los nombres salen de las historias
 * HU-01, HU-02 y HU-08 y de los eventos "Instrumento registrado en el catálogo"
 * y "Precio cotizado actualizado". No reutilizamos palabras de Órdenes
 * (precio límite, emparejamiento) ni de Portafolio (posición, cantidad).
 */
public final class LenguajeUbicuoMercado {

    private LenguajeUbicuoMercado() {
    }

    /**
     * Glosario del contexto. La clave es el término; el valor es su significado
     * dentro de Mercado y en ningún otro lado.
     */
    public static Map<String, String> terminos() {
        return Map.of(
                "Instrumento",
                "Activo del catálogo (acción o ETF) que el inversionista consulta antes de operar.",
                "Simbolo",
                "Identidad del instrumento. Otros contextos la guardan como String, nunca como objeto Instrumento.",
                "TipoInstrumento",
                "Clase del activo en el catálogo: ACCION o ETF.",
                "Cotizacion",
                "Precio vigente de un instrumento en un momento concreto.",
                "PrecioCotizado",
                "Valor de una cotización: siempre mayor a cero y con marca de tiempo.",
                "Historico",
                "Serie de precios cotizados de un mismo instrumento, ordenada en el tiempo.",
                "SimulacionDePrecios",
                "Actualización periódica de todo el catálogo para que el mercado se sienta vivo, sin una bolsa real (HU-08)."
        );
    }
}
