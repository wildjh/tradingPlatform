package com.g1.trading.mercado;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Paso 1. El glosario usa los términos de HU-01, HU-02 y HU-08,
 * y no mezcla palabras de Órdenes ni de Portafolio.
 */
class LenguajeUbicuoMercadoTest {

    @Test
    void defineLosTerminosDelCatalogo() {
        assertThat(LenguajeUbicuoMercado.terminos()).containsKeys(
                "Instrumento",
                "Simbolo",
                "TipoInstrumento",
                "Cotizacion",
                "PrecioCotizado",
                "Historico",
                "SimulacionDePrecios"
        );
    }

    @Test
    void elSimboloEsLaReferenciaPorIdHaciaOtrosContextos() {
        assertThat(LenguajeUbicuoMercado.terminos().get("Simbolo"))
                .contains("String")
                .contains("nunca como objeto Instrumento");
    }
}
