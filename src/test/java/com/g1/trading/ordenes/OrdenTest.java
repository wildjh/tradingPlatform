package com.g1.trading.ordenes;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrdenTest {

    private Orden crearOrdenLimite() {
        return OrdenFactory.crearLimite(
                "inversionista-1",
                "instrumento-1",
                LadoOrden.COMPRA,
                new BigDecimal("5"),
                new BigDecimal("100")
        );
    }

    @Test
    void unaOrdenLimiteNacePendiente() {
        Orden orden = crearOrdenLimite();

        assertAll(
                () -> assertEquals(
                        EstadoOrden.PENDIENTE, orden.estado()
                ),
                () -> assertTrue(orden.estaPendiente()),
                () -> assertEquals(TipoOrden.LIMITE, orden.tipo()),
                () -> assertEquals(LadoOrden.COMPRA, orden.lado()),
                () -> assertNotNull(orden.precioLimite()),
                () -> assertFalse(orden.id().isBlank())
        );
    }

    @Test
    void unaOrdenMercadoNaceSinPrecioLimite() {
        Orden orden = OrdenFactory.crearMercado(
                "inversionista-1",
                "instrumento-1",
                LadoOrden.VENTA,
                BigDecimal.ONE
        );

        assertAll(
                () -> assertEquals(TipoOrden.MERCADO, orden.tipo()),
                () -> assertTrue(orden.estaPendiente()),
                () -> assertNull(orden.precioLimite())
        );
    }

    @Test
    void permiteCancelarUnaOrdenPendiente() {
        Orden orden = crearOrdenLimite();

        orden.cancelar();

        assertEquals(EstadoOrden.CANCELADA, orden.estado());
        assertFalse(orden.estaPendiente());
    }

    @Test
    void permiteEjecutarUnaOrdenPendiente() {
        Orden orden = crearOrdenLimite();

        orden.ejecutar();

        assertEquals(EstadoOrden.EJECUTADA, orden.estado());
        assertFalse(orden.estaPendiente());
    }

    @Test
    void unaOrdenEjecutadaNoPuedeCancelarseNiEjecutarseOtraVez() {
        Orden orden = crearOrdenLimite();
        orden.ejecutar();

        assertAll(
                () -> assertThrows(
                        IllegalStateException.class,
                        orden::cancelar
                ),
                () -> assertThrows(
                        IllegalStateException.class,
                        orden::ejecutar
                ),
                () -> assertEquals(
                        EstadoOrden.EJECUTADA, orden.estado()
                )
        );
    }

    @Test
    void unaOrdenCanceladaNoPuedeEjecutarseNiCancelarseOtraVez() {
        Orden orden = crearOrdenLimite();
        orden.cancelar();

        assertAll(
                () -> assertThrows(
                        IllegalStateException.class,
                        orden::ejecutar
                ),
                () -> assertThrows(
                        IllegalStateException.class,
                        orden::cancelar
                ),
                () -> assertEquals(
                        EstadoOrden.CANCELADA, orden.estado()
                )
        );
    }

    @Test
    void rechazaCantidadesNulasCeroYNegativas() {
        BigDecimal[] cantidades = {
                null,
                BigDecimal.ZERO,
                new BigDecimal("-1")
        };

        for (BigDecimal cantidad : cantidades) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> OrdenFactory.crearMercado(
                            "inversionista-1",
                            "instrumento-1",
                            LadoOrden.COMPRA,
                            cantidad
                    )
            );
        }
    }

    @Test
    void rechazaUnaOrdenLimiteSinPrecio() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Orden(
                        "orden-1",
                        "inversionista-1",
                        "instrumento-1",
                        LadoOrden.COMPRA,
                        TipoOrden.LIMITE,
                        BigDecimal.ONE,
                        null
                )
        );
    }

    @Test
    void rechazaUnaOrdenMercadoConPrecioLimite() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Orden(
                        "orden-1",
                        "inversionista-1",
                        "instrumento-1",
                        LadoOrden.COMPRA,
                        TipoOrden.MERCADO,
                        BigDecimal.ONE,
                        new PrecioLimite(new BigDecimal("100"))
                )
        );
    }

    @Test
    void rechazaUnInstrumentoSinIdentificador() {
        assertThrows(
                IllegalArgumentException.class,
                () -> OrdenFactory.crearMercado(
                        "inversionista-1",
                        " ",
                        LadoOrden.COMPRA,
                        BigDecimal.ONE
                )
        );
    }
}