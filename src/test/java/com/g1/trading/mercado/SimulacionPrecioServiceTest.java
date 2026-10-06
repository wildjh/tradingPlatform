package com.g1.trading.mercado;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimulacionPrecioServiceTest {

    private final SimulacionPrecioService servicio = new SimulacionPrecioService();
    private final InstrumentoFactory factory = new InstrumentoFactory();
    private final Instant apertura = Instant.parse("2026-10-06T13:00:00Z");
    private final Instant simulacion = Instant.parse("2026-10-06T13:05:00Z");

    @Test
    void aplicaElMismoFactorYLaMismaMarcaDeTiempoATodoElCatalogo() {
        Instrumento apple = factory.registrar("AAPL", "Apple", TipoInstrumento.ACCION, new BigDecimal("100"), apertura);
        Instrumento spy = factory.registrar("SPY", "S&P 500", TipoInstrumento.ETF, new BigDecimal("50"), apertura);

        servicio.simular(java.util.List.of(apple, spy), new BigDecimal("1.02"), simulacion);

        assertThat(apple.cotizacionActual().valor()).isEqualByComparingTo("102.0000");
        assertThat(spy.cotizacionActual().valor()).isEqualByComparingTo("51.0000");
        assertThat(apple.cotizacionActual().momento()).isEqualTo(simulacion);
        assertThat(spy.cotizacionActual().momento()).isEqualTo(simulacion);
        assertThat(apple.historico()).hasSize(2);
        assertThat(spy.historico()).hasSize(2);
    }

    @Test
    void siUnaCotizacionNoPuedeAvanzarNoModificaANingunInstrumento() {
        Instrumento apple = factory.registrar("AAPL", "Apple", TipoInstrumento.ACCION, new BigDecimal("100"), apertura);
        Instrumento spy = factory.registrar(
                "SPY", "S&P 500", TipoInstrumento.ETF, new BigDecimal("50"), simulacion);

        assertThatThrownBy(() -> servicio.simular(
                java.util.List.of(apple, spy), new BigDecimal("1.02"), simulacion))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SPY");

        assertThat(apple.cotizacionActual().valor()).isEqualByComparingTo("100.0000");
        assertThat(apple.historico()).hasSize(1);
        assertThat(spy.historico()).hasSize(1);
    }

    @Test
    void rechazaFactorCeroONegativo() {
        Instrumento apple = factory.registrar("AAPL", "Apple", TipoInstrumento.ACCION, new BigDecimal("100"), apertura);

        assertThatThrownBy(() -> servicio.simular(java.util.List.of(apple), BigDecimal.ZERO, simulacion))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("factor");
        assertThatThrownBy(() -> servicio.simular(java.util.List.of(apple), new BigDecimal("-0.5"), simulacion))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("factor");
    }

    @Test
    void rechazaCatalogoVacioYMomentoNulo() {
        assertThatThrownBy(() -> servicio.simular(java.util.List.of(), new BigDecimal("1.01"), simulacion))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("catálogo");

        Instrumento apple = factory.registrar("AAPL", "Apple", TipoInstrumento.ACCION, new BigDecimal("100"), apertura);
        assertThatThrownBy(() -> servicio.simular(java.util.List.of(apple), new BigDecimal("1.01"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("marca de tiempo");
    }

    @Test
    void rechazaUnFactorQueDejaElPrecioEnCeroYNoTocaElCatalogo() {
        Instrumento penny = factory.registrar("ABC", "Penny", TipoInstrumento.ACCION, new BigDecimal("0.0001"), apertura);

        assertThatThrownBy(() -> servicio.simular(java.util.List.of(penny), new BigDecimal("0.01"), simulacion))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor a cero");

        assertThat(penny.cotizacionActual().valor()).isEqualByComparingTo("0.0001");
        assertThat(penny.historico()).hasSize(1);
    }
}
