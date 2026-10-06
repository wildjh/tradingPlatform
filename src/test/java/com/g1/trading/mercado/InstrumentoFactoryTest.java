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
    void registraUnInstrumentoValidoConPrecioYTrazabilidad() {
        Instrumento instrumento = factory.registrar(
                " aapl ", " Apple ", TipoInstrumento.ACCION,
                new BigDecimal("189.5"), apertura, "admin-mercado");

        assertThat(instrumento.id().valor()).isEqualTo("AAPL");
        assertThat(instrumento.nombre()).isEqualTo("Apple");
        assertThat(instrumento.tipo()).isEqualTo(TipoInstrumento.ACCION);

        Cotizacion actual = instrumento.obtenerCotizacionActual();
        assertThat(actual.precio().valor()).isEqualByComparingTo("189.5000");
        assertThat(actual.precio().momento()).isEqualTo(apertura);
        assertThat(actual.origen()).isEqualTo(OrigenCotizacion.CARGA_INICIAL);
        assertThat(actual.generadoPor()).isEqualTo("admin-mercado");
        assertThat(instrumento.consultarHistorico().cotizaciones()).containsExactly(actual);
    }

    @Test
    void registraUnEtf() {
        Instrumento etf = factory.registrar(
                "SPY", "S&P 500", TipoInstrumento.ETF, new BigDecimal("500"), apertura, "admin-mercado");

        assertThat(etf.tipo()).isEqualTo(TipoInstrumento.ETF);
        assertThat(etf.id().valor()).isEqualTo("SPY");
    }

    @Test
    void rechazaSimboloVacioDemasiadoLargoOConNumeros() {
        assertThatThrownBy(() -> factory.registrar(
                "  ", "Apple", TipoInstrumento.ACCION, new BigDecimal("10"), apertura, "admin-mercado"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("símbolo");
        assertThatThrownBy(() -> factory.registrar(
                "APPLE1", "Apple", TipoInstrumento.ACCION, new BigDecimal("10"), apertura, "admin-mercado"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("5 letras");
        assertThatThrownBy(() -> factory.registrar(
                null, "Apple", TipoInstrumento.ACCION, new BigDecimal("10"), apertura, "admin-mercado"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaNombreVacioTipoNuloPrecioNoPositivoYAutorVacio() {
        assertThatThrownBy(() -> factory.registrar(
                "AAPL", "  ", TipoInstrumento.ACCION, new BigDecimal("10"), apertura, "admin-mercado"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre");
        assertThatThrownBy(() -> factory.registrar(
                "AAPL", "Apple", null, new BigDecimal("10"), apertura, "admin-mercado"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tipo");
        assertThatThrownBy(() -> factory.registrar(
                "AAPL", "Apple", TipoInstrumento.ACCION, BigDecimal.ZERO, apertura, "admin-mercado"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor a cero");
        assertThatThrownBy(() -> factory.registrar(
                "AAPL", "Apple", TipoInstrumento.ACCION, new BigDecimal("10"), null, "admin-mercado"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("marca de tiempo");
        assertThatThrownBy(() -> factory.registrar(
                "AAPL", "Apple", TipoInstrumento.ACCION, new BigDecimal("10"), apertura, "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quién");
    }
}
