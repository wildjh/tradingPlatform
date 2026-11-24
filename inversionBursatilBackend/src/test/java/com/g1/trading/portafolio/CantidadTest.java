package com.g1.trading.portafolio;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CantidadTest {

    @Test
    void aceptaCeroYPositivos() {
        assertThat(Cantidad.de(new BigDecimal("5")).valor()).isEqualByComparingTo("5");
        assertThat(Cantidad.CERO.esCero()).isTrue();
    }

    @Test
    void rechazaNulaYNegativa() {
        assertThatThrownBy(() -> Cantidad.de(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Cantidad.de(new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("negativa");
        assertThatThrownBy(() -> Cantidad.de(new BigDecimal("-0.00001")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void mismaCantidadConDistintaEscalaEsIgual() {
        assertThat(Cantidad.de(new BigDecimal("10"))).isEqualTo(Cantidad.de(new BigDecimal("10.0")));
    }

    @Test
    void sumaYRestaSinPermitirNegativos() {
        Cantidad cinco = Cantidad.de(new BigDecimal("5"));
        Cantidad dos = Cantidad.de(new BigDecimal("2"));
        assertThat(cinco.sumar(dos).valor()).isEqualByComparingTo("7");
        assertThat(cinco.restar(dos).valor()).isEqualByComparingTo("3");
        assertThatThrownBy(() -> dos.restar(cinco)).isInstanceOf(IllegalArgumentException.class);
    }
}
