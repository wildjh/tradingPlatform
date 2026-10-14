package com.g1.trading.portafolio;


import org.junit.jupiter.api.Test;

import com.g1.trading.mercado.InstrumentoId;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PortafolioTest {

    private final InstrumentoId aapl = new InstrumentoId("AAPL");
    private final Portafolio portafolio = PortafolioFactory.crearVacio("inv-1");

    private static Cantidad c(String v) {
        return Cantidad.de(new BigDecimal(v));
    }

    @Test
    void lasComprasAcumulanCantidadYCostoPromedio() {
        portafolio.registrarCompra(aapl, c("10"), new BigDecimal("100"));
        portafolio.registrarCompra(aapl, c("10"), new BigDecimal("120"));

        assertThat(portafolio.cantidadDisponible(aapl).valor()).isEqualByComparingTo("20");
        assertThat(portafolio.posiciones().get(0).costoPromedio()).isEqualByComparingTo("110");
    }

    @Test
    void calculaValorTotalYGananciaNoRealizada() {
        portafolio.registrarCompra(aapl, c("10"), new BigDecimal("100"));
        portafolio.registrarCompra(aapl, c("10"), new BigDecimal("120"));
        Map<InstrumentoId, BigDecimal> precios = Map.of(aapl, new BigDecimal("130"));

        assertThat(portafolio.valorTotal(precios)).isEqualByComparingTo("2600");
        assertThat(portafolio.gananciaNoRealizada(precios)).isEqualByComparingTo("400");
    }

    @Test
    void noSePuedeVenderMasDeLoQueSePosee() {
        portafolio.registrarCompra(aapl, c("5"), new BigDecimal("100"));

        assertThatThrownBy(() -> portafolio.registrarVenta(aapl, c("6"), new BigDecimal("100")))
                .isInstanceOf(VentaNoPermitidaException.class);
        assertThat(portafolio.cantidadDisponible(aapl).valor()).isEqualByComparingTo("5");
    }

    @Test
    void ventaTotalEliminaLaPosicion() {
        portafolio.registrarCompra(aapl, c("3"), new BigDecimal("50"));
        portafolio.registrarVenta(aapl, c("3"), new BigDecimal("55"));

        assertThat(portafolio.posiciones()).isEmpty();
    }

    @Test
    void rechazaCantidadCeroYPrecioNoPositivo() {
        assertThatThrownBy(() -> portafolio.registrarCompra(aapl, Cantidad.CERO, new BigDecimal("10")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> portafolio.registrarCompra(aapl, c("1"), BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void consultarPosicionesNoPermiteAlterarElAgregado() {
        portafolio.registrarCompra(aapl, c("1"), new BigDecimal("10"));

        assertThatThrownBy(() -> portafolio.posiciones().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void faltaPrecioActualEsError() {
        portafolio.registrarCompra(aapl, c("1"), new BigDecimal("10"));

        assertThatThrownBy(() -> portafolio.valorTotal(Map.of()))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("AAPL");
    }
}
