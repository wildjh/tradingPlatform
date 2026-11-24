package com.g1.trading.portafolio;

import org.junit.jupiter.api.Test;

import com.g1.trading.mercado.InstrumentoId;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidacionVentaServiceTest {

    private final ValidacionVentaService servicio = new ValidacionVentaService();
    private final InstrumentoId aapl = new InstrumentoId("AAPL");
    private final Portafolio portafolio = PortafolioFactory.crearVacio("inv-1");

    private static Cantidad c(String v) {
        return Cantidad.de(new BigDecimal(v));
    }

    @Test
    void permiteVenderHastaLaCantidadPoseida() {
        portafolio.registrarCompra(aapl, c("5"), new BigDecimal("100"));

        assertThat(servicio.puedeVender(portafolio, aapl, c("5"))).isTrue();
        assertThat(servicio.puedeVender(portafolio, aapl, c("6"))).isFalse();
    }

    @Test
    void rechazaVenderLoQueNoSePosee() {
        assertThatThrownBy(() -> servicio.validar(portafolio, aapl, c("1")))
                .isInstanceOf(VentaNoPermitidaException.class).hasMessageContaining("AAPL");
    }

    @Test
    void rechazaVenderCantidadCeroYArgumentosNulos() {
        portafolio.registrarCompra(aapl, c("5"), new BigDecimal("100"));

        assertThat(servicio.puedeVender(portafolio, aapl, Cantidad.CERO)).isFalse();
        assertThatThrownBy(() -> servicio.puedeVender(null, aapl, c("1")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validarNoModificaElPortafolio() {
        portafolio.registrarCompra(aapl, c("5"), new BigDecimal("100"));
        servicio.validar(portafolio, aapl, c("5"));

        assertThat(portafolio.cantidadDisponible(aapl).valor()).isEqualByComparingTo("5");
    }
}
