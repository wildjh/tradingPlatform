package com.g1.trading.mercado;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InstrumentoTest {

    private final Instant apertura = Instant.parse("2026-10-06T13:00:00Z");
    private final PrecioCotizado inicial = new PrecioCotizado(new BigDecimal("80"), apertura);

    @Test
    void naceConLaCotizacionInicialDentroDelHistorico() {
        Instrumento instrumento = new Instrumento("AAPL", "Apple", TipoInstrumento.ACCION, inicial);

        assertThat(instrumento.simbolo()).isEqualTo("AAPL");
        assertThat(instrumento.cotizacionActual()).isEqualTo(inicial);
        assertThat(instrumento.historico()).containsExactly(inicial);
    }

    @Test
    void laNuevaCotizacionDebeSerPosteriorALaUltima() {
        Instrumento instrumento = new Instrumento("AAPL", "Apple", TipoInstrumento.ACCION, inicial);
        Instant despues = apertura.plusSeconds(60);

        instrumento.registrarCotizacion(new PrecioCotizado(new BigDecimal("81"), despues));

        assertThat(instrumento.cotizacionActual().valor()).isEqualByComparingTo("81");
        assertThat(instrumento.historico()).hasSize(2);
    }

    @Test
    void rechazaUnaCotizacionConLaMismaMarcaDeTiempoOAnterior() {
        Instrumento instrumento = new Instrumento("AAPL", "Apple", TipoInstrumento.ACCION, inicial);

        assertThatThrownBy(() -> instrumento.registrarCotizacion(
                new PrecioCotizado(new BigDecimal("81"), apertura)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("posterior");

        assertThatThrownBy(() -> instrumento.registrarCotizacion(
                new PrecioCotizado(new BigDecimal("81"), apertura.minusSeconds(1))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("posterior");

        assertThat(instrumento.historico()).hasSize(1);
    }

    @Test
    void elHistoricoDevueltoNoPermiteAlterarElAgregado() {
        Instrumento instrumento = new Instrumento("AAPL", "Apple", TipoInstrumento.ACCION, inicial);
        List<PrecioCotizado> consulta = instrumento.historico();

        assertThatThrownBy(() -> consulta.add(new PrecioCotizado(new BigDecimal("90"), apertura.plusSeconds(1))))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(instrumento.historico()).hasSize(1);
    }

    @Test
    void dosInstrumentosConElMismoSimboloSonLaMismaEntidad() {
        Instrumento uno = new Instrumento("AAPL", "Apple", TipoInstrumento.ACCION, inicial);
        Instrumento otro = new Instrumento(
                "AAPL", "Apple Inc.", TipoInstrumento.ACCION,
                new PrecioCotizado(new BigDecimal("90"), apertura.plusSeconds(10)));

        assertThat(uno).isEqualTo(otro);
        assertThat(uno.hashCode()).isEqualTo(otro.hashCode());
    }

    @Test
    void rechazaSimboloNombreOCotizacionInvalidos() {
        assertThatThrownBy(() -> new Instrumento("aapl", "Apple", TipoInstrumento.ACCION, inicial))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Instrumento("AAPL", "   ", TipoInstrumento.ACCION, inicial))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Instrumento("AAPL", "Apple", null, inicial))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Instrumento("AAPL", "Apple", TipoInstrumento.ACCION, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
