package com.g1.trading.portafolio;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PortafolioFactoryTest {

    @Test
    void creaUnPortafolioVacioConIdLimpio() {
        Portafolio portafolio = PortafolioFactory.crearVacio("  inv-1 ");

        assertThat(portafolio.inversionistaId().valor()).isEqualTo("inv-1");
        assertThat(portafolio.posiciones()).isEmpty();
    }

    @Test
    void rechazaInversionistaNuloOVacio() {
        assertThatThrownBy(() -> PortafolioFactory.crearVacio(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PortafolioFactory.crearVacio("  "))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("inversionista");
    }

    @Test
    void noHayConstructorPublico() {
        assertThat(Portafolio.class.getConstructors()).isEmpty();
        assertThat(Modifier.isPublic(Portafolio.class.getDeclaredConstructors()[0].getModifiers())).isFalse();
    }
}
