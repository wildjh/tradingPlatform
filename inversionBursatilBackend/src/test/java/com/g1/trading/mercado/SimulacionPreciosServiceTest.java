package com.g1.trading.mercado;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimulacionPreciosServiceTest {

    private final SimulacionPreciosService servicio = new SimulacionPreciosService();
    private final InstrumentoFactory factory = new InstrumentoFactory();
    private final Instant apertura = Instant.parse("2026-10-06T13:00:00Z");
    private final Instant simulacion = Instant.parse("2026-10-06T13:05:00Z");

    @Test
    void actualizaElPrecioDeTodoElCatalogoConLaMismaTrazabilidad() {
        Instrumento apple = instrumento("AAPL", "Apple", TipoInstrumento.ACCION, "100");
        Instrumento spy = instrumento("SPY", "S&P 500", TipoInstrumento.ETF, "50");

        servicio.simular(List.of(apple, spy), new BigDecimal("1.02"), simulacion, "admin-mercado");

        assertThat(apple.obtenerCotizacionActual().precio().valor()).isEqualByComparingTo("102.0000");
        assertThat(spy.obtenerCotizacionActual().precio().valor()).isEqualByComparingTo("51.0000");
        assertThat(apple.obtenerCotizacionActual().precio().momento()).isEqualTo(simulacion);
        assertThat(apple.obtenerCotizacionActual().origen()).isEqualTo(OrigenCotizacion.SIMULACION);
        assertThat(apple.obtenerCotizacionActual().generadoPor()).isEqualTo("admin-mercado");
        assertThat(spy.obtenerCotizacionActual().origen()).isEqualTo(OrigenCotizacion.SIMULACION);
        assertThat(apple.consultarHistorico().cotizaciones()).hasSize(2);
        assertThat(spy.consultarHistorico().cotizaciones()).hasSize(2);
    }

    @Test
    void siUnaCotizacionNoPuedeAvanzarNoModificaANingunInstrumento() {
        Instrumento apple = instrumento("AAPL", "Apple", TipoInstrumento.ACCION, "100");
        Instrumento spy = factory.registrar(
                "SPY", "S&P 500", TipoInstrumento.ETF, new BigDecimal("50"), simulacion, "admin-mercado");

        assertThatThrownBy(() -> servicio.simular(
                List.of(apple, spy), new BigDecimal("1.02"), simulacion, "admin-mercado"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SPY");

        assertThat(apple.obtenerCotizacionActual().precio().valor()).isEqualByComparingTo("100.0000");
        assertThat(apple.consultarHistorico().cotizaciones()).hasSize(1);
        assertThat(spy.consultarHistorico().cotizaciones()).hasSize(1);
    }

    @Test
    void rechazaFactorCeroONegativo() {
        Instrumento apple = instrumento("AAPL", "Apple", TipoInstrumento.ACCION, "100");

        assertThatThrownBy(() -> servicio.simular(List.of(apple), BigDecimal.ZERO, simulacion, "admin-mercado"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("factor");
        assertThatThrownBy(() -> servicio.simular(List.of(apple), new BigDecimal("-0.5"), simulacion, "admin-mercado"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("factor");
    }

    @Test
    void rechazaCatalogoVacioMomentoNuloYAutorVacio() {
        assertThatThrownBy(() -> servicio.simular(List.of(), new BigDecimal("1.01"), simulacion, "admin-mercado"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("catálogo");

        Instrumento apple = instrumento("AAPL", "Apple", TipoInstrumento.ACCION, "100");
        assertThatThrownBy(() -> servicio.simular(List.of(apple), new BigDecimal("1.01"), null, "admin-mercado"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("marca de tiempo");
        assertThatThrownBy(() -> servicio.simular(List.of(apple), new BigDecimal("1.01"), simulacion, " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quién");
    }

    @Test
    void rechazaUnFactorQueDejaElPrecioEnCeroYNoTocaElCatalogo() {
        Instrumento penny = instrumento("ABC", "Penny", TipoInstrumento.ACCION, "0.0001");

        assertThatThrownBy(() -> servicio.simular(List.of(penny), new BigDecimal("0.01"), simulacion, "admin-mercado"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor a cero");

        assertThat(penny.obtenerCotizacionActual().precio().valor()).isEqualByComparingTo("0.0001");
        assertThat(penny.consultarHistorico().cotizaciones()).hasSize(1);
    }

    private Instrumento instrumento(String simbolo, String nombre, TipoInstrumento tipo, String precio) {
        return factory.registrar(simbolo, nombre, tipo, new BigDecimal(precio), apertura, "admin-mercado");
    }
}
