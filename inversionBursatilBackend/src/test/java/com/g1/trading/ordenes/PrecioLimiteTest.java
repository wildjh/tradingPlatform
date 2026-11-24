package com.g1.trading.ordenes;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PrecioLimiteTest {

    @Test
    void aceptaUnPrecioPositivo() {
        PrecioLimite precio = new PrecioLimite(
                new BigDecimal("1500")
        );

        assertEquals(new BigDecimal("1500.0000"), precio.valor());
    }

    @Test
    void rechazaUnPrecioNulo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PrecioLimite(null)
        );
    }

    @Test
    void rechazaCeroYPreciosNegativos() {
        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new PrecioLimite(BigDecimal.ZERO)
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new PrecioLimite(new BigDecimal("-10"))
                )
        );
    }

    @Test
    void rechazaUnPrecioQueSeRedondeaACero() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PrecioLimite(new BigDecimal("0.00001"))
        );
    }

    @Test
    void preciosConDistintaEscalaSonIguales() {
        PrecioLimite primero = new PrecioLimite(
                new BigDecimal("10")
        );
        PrecioLimite segundo = new PrecioLimite(
                new BigDecimal("10.0")
        );

        assertEquals(primero, segundo);
        assertEquals(primero.hashCode(), segundo.hashCode());
    }
}