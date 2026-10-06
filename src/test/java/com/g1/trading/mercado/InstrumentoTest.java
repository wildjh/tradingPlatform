package com.g1.trading.mercado;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InstrumentoTest {

    private final InstrumentoFactory factory = new InstrumentoFactory();
    private final Instant apertura = Instant.parse("2026-10-06T13:00:00Z");

    @Test
    void siempreNaceConUnaCotizacionValida() {
        Instrumento instrumento = apple();

        Cotizacion actual = instrumento.obtenerCotizacionActual();
        assertThat(actual.precio().valor()).isEqualByComparingTo("80");
        assertThat(instrumento.consultarHistorico().cotizaciones()).containsExactly(actual);
    }

    @Test
    void actualizarPrecioExigeUnMomentoPosteriorYDejaTrazabilidad() {
        Instrumento instrumento = apple();
        Instant despues = apertura.plusSeconds(60);

        instrumento.actualizarPrecio(
                new PrecioCotizado(new BigDecimal("81"), despues),
                OrigenCotizacion.SIMULACION,
                "admin-mercado");

        Cotizacion actual = instrumento.obtenerCotizacionActual();
        assertThat(actual.precio().valor()).isEqualByComparingTo("81");
        assertThat(actual.precio().momento()).isEqualTo(despues);
        assertThat(actual.origen()).isEqualTo(OrigenCotizacion.SIMULACION);
        assertThat(actual.generadoPor()).isEqualTo("admin-mercado");
        assertThat(instrumento.consultarHistorico().cotizaciones()).hasSize(2);
    }

    @Test
    void rechazaUnaCotizacionConLaMismaMarcaDeTiempoOAnterior() {
        Instrumento instrumento = apple();

        assertThatThrownBy(() -> instrumento.actualizarPrecio(
                new PrecioCotizado(new BigDecimal("81"), apertura),
                OrigenCotizacion.SIMULACION,
                "admin-mercado"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("posterior");

        assertThatThrownBy(() -> instrumento.actualizarPrecio(
                new PrecioCotizado(new BigDecimal("81"), apertura.minusSeconds(1)),
                OrigenCotizacion.SIMULACION,
                "admin-mercado"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("posterior");

        assertThat(instrumento.consultarHistorico().cotizaciones()).hasSize(1);
    }

    @Test
    void consultarHistoricoNoPermiteAlterarElAgregado() {
        Instrumento instrumento = apple();
        List<Cotizacion> consulta = instrumento.consultarHistorico().cotizaciones();

        assertThatThrownBy(() -> consulta.add(instrumento.obtenerCotizacionActual()))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(instrumento.consultarHistorico().cotizaciones()).hasSize(1);
    }

    @Test
    void dosInstrumentosConElMismoIdSonLaMismaEntidad() {
        Instrumento uno = apple();
        Instrumento otro = factory.registrar(
                "AAPL", "Apple Inc.", TipoInstrumento.ACCION,
                new BigDecimal("90"), apertura.plusSeconds(10), "otro-admin");

        assertThat(uno).isEqualTo(otro);
        assertThat(uno.hashCode()).isEqualTo(otro.hashCode());
    }

    @Test
    void noHayConstructorPublico() {
        for (Constructor<?> constructor : Instrumento.class.getConstructors()) {
            assertThat(Modifier.isPublic(constructor.getModifiers())).isFalse();
        }
        assertThat(Instrumento.class.getConstructors()).isEmpty();
    }

    private Instrumento apple() {
        return factory.registrar(
                "AAPL", "Apple", TipoInstrumento.ACCION, new BigDecimal("80"), apertura, "admin-mercado");
    }
}
