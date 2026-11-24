package com.g1.trading.mercado.dominio;

/**
 * Qué generó una cotización. Junto con quién y el momento del PrecioCotizado,
 * deja la actualización trazable sin salir del contexto Mercado.
 */
public enum OrigenCotizacion {
    CARGA_INICIAL,
    SIMULACION
}
