package com.g1.trading.mercado;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrecioCotizadoTest {

    private final Instant ahora = Instant.parse("2026-10-06T14:00:00Z");

    @Test
    void aceptaUnValorPositivoConMarcaDeTiempo() {
        PrecioCotizado precio = new PrecioCotizado(new BigDecimal("125.50"), ahora);

        assertThat(precio.valor()).isEqualByComparingTo("125.5000");
        assertThat(precio.momento()).isEqualTo(ahora);
    }

    @Test
    void dosCotizacionesConElMismoValorYMomentoSonIguales() {
        PrecioCotizado una = new PrecioCotizado(new BigDecimal("10"), ahora);
        PrecioCotizado otra = new PrecioCotizado(new BigDecimal("10.0000"), ahora);

        assertThat(una).isEqualTo(otra);
    }

    @Test
    void rechazaPrecioCero() {
        assertThatThrownBy(() -> new PrecioCotizado(BigDecimal.ZERO, ahora))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor a cero");
    }

    @Test
    void rechazaPrecioNegativo() {
        assertThatThrownBy(() -> new PrecioCotizado(new BigDecimal("-1"), ahora))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor a cero");
    }

    @Test
    void rechazaValorNulo() {
        assertThatThrownBy(() -> new PrecioCotizado(null, ahora))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valor");
    }

    @Test
    void rechazaMomentoNulo() {
        assertThatThrownBy(() -> new PrecioCotizado(new BigDecimal("10"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("marca de tiempo");
    }

    @Test
    void rechazaUnValorQueAlRedondearQuedaEnCero() {
        assertThatThrownBy(() -> new PrecioCotizado(new BigDecimal("0.00001"), ahora))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor a cero");
    }
}
