package com.g1.trading.mercado;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LenguajeUbicuoMercadoTest {

    @Test
    void defineLosTerminosDelCatalogo() {
        assertThat(LenguajeUbicuoMercado.terminos()).containsKeys(
                "Instrumento",
                "InstrumentoId",
                "TipoInstrumento",
                "Cotizacion",
                "PrecioCotizado",
                "HistoricoPrecios",
                "SimulacionPrecios"
        );
    }

    @Test
    void elIdEsLaReferenciaHaciaOtrosContextos() {
        assertThat(LenguajeUbicuoMercado.terminos().get("InstrumentoId"))
                .contains("String")
                .contains("nunca el objeto Instrumento");
    }
}
