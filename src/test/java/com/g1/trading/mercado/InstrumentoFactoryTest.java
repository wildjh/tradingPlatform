package com.g1.trading.mercado;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InstrumentoFactoryTest {

    private final InstrumentoFactory factory = new InstrumentoFactory();
    private final Instant apertura = Instant.parse("2026-10-06T13:00:00Z");

    @Test
    void registraUnInstrumentoConSimboloNormalizadoYCotizacionInicial() {
        Instrumento instrumento = factory.registrar(
                " aapl ", " Apple ", TipoInstrumento.ACCION, new BigDecimal("189.5"), apertura);

        assertThat(instrumento.simbolo()).isEqualTo("AAPL");
        assertThat(instrumento.nombre()).isEqualTo("Apple");
        assertThat(instrumento.tipo()).isEqualTo(TipoInstrumento.ACCION);
        assertThat(instrumento.cotizacionActual().valor()).isEqualByComparingTo("189.5000");
        assertThat(instrumento.cotizacionActual().momento()).isEqualTo(apertura);
        assertThat(instrumento.historico()).hasSize(1);
    }

    @Test
    void registraUnEtf() {
        Instrumento etf = factory.registrar("SPY", "S&P 500", TipoInstrumento.ETF, new BigDecimal("500"), apertura);

        assertThat(etf.tipo()).isEqualTo(TipoInstrumento.ETF);
        assertThat(etf.simbolo()).isEqualTo("SPY");
    }

    @Test
    void rechazaSimboloVacioDemasiadoLargoOConNumeros() {
        assertThatThrownBy(() -> factory.registrar("  ", "Apple", TipoInstrumento.ACCION, new BigDecimal("10"), apertura))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("símbolo");
        assertThatThrownBy(() -> factory.registrar("APPLE1", "Apple", TipoInstrumento.ACCION, new BigDecimal("10"), apertura))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("5 letras");
        assertThatThrownBy(() -> factory.registrar(null, "Apple", TipoInstrumento.ACCION, new BigDecimal("10"), apertura))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaNombreVacioTipoNuloYPrecioNoPositivo() {
        assertThatThrownBy(() -> factory.registrar("AAPL", "  ", TipoInstrumento.ACCION, new BigDecimal("10"), apertura))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre");
        assertThatThrownBy(() -> factory.registrar("AAPL", "Apple", null, new BigDecimal("10"), apertura))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tipo");
        assertThatThrownBy(() -> factory.registrar("AAPL", "Apple", TipoInstrumento.ACCION, BigDecimal.ZERO, apertura))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor a cero");
        assertThatThrownBy(() -> factory.registrar("AAPL", "Apple", TipoInstrumento.ACCION, new BigDecimal("10"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("marca de tiempo");
    }
}
