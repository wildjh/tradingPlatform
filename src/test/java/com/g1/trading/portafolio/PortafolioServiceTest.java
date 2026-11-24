package com.g1.trading.portafolio;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PortafolioServiceTest {

    private final PortafolioService servicio = new PortafolioService(new PortafolioRepositoryEnMemoria());

    private static BigDecimal n(String v) {
        return new BigDecimal(v);
    }

    private void sembrarVendedor() {
        servicio.cargarPosicionInicial("vendedor-1", "AAPL", n("10"), n("100"));
    }

    @Test
    void transaccionActualizaAmbosPortafoliosEnLaMismaOperacion() {
        sembrarVendedor();

        servicio.aplicarTransaccion("comprador-1", "vendedor-1", "aapl", n("4"), n("110"));

        ResumenPortafolio comprador = servicio.consultar("comprador-1", Map.of("AAPL", n("120")));
        ResumenPortafolio vendedor = servicio.consultar("vendedor-1", Map.of("AAPL", n("120")));
        assertThat(comprador.valorTotal()).isEqualByComparingTo("480");
        assertThat(comprador.gananciaNoRealizada()).isEqualByComparingTo("40");
        assertThat(vendedor.posiciones().get(0).cantidad().valor()).isEqualByComparingTo("6");
    }

    @Test
    void siElVendedorNoTieneCantidadNoSeTocaAlComprador() {
        assertThatThrownBy(() -> servicio.aplicarTransaccion(
                "comprador-1", "vendedor-1", "AAPL", n("1"), n("100")))
                .isInstanceOf(VentaNoPermitidaException.class);

        assertThat(servicio.consultar("comprador-1", Map.of()).posiciones()).isEmpty();
    }

    @Test
    void validarVentaRechazaSinPosesionYAceptaConPosesion() {
        sembrarVendedor();

        assertThatThrownBy(() -> servicio.validarVenta("comprador-1", "AAPL", n("1")))
                .isInstanceOf(VentaNoPermitidaException.class);
        servicio.validarVenta("vendedor-1", "AAPL", n("10"));
    }

    @Test
    void rechazaIdsVacios() {
        assertThatThrownBy(() -> servicio.consultar(" ", Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> servicio.validarVenta("inv-1", null, n("1")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
