package com.g1.trading.ordenes;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmparejamientoServiceTest {

    private final EmparejamientoService servicio =
            new EmparejamientoService();

    private Orden crearOrden(
            String instrumentoId,
            LadoOrden lado,
            String precio
    ) {
        return OrdenFactory.crearLimite(
                lado == LadoOrden.COMPRA ? "comprador-1" : "vendedor-1",
                instrumentoId,
                lado,
                new BigDecimal("5"),
                new BigDecimal(precio)
        );
    }

    @Test
    void aceptaCompraConPrecioMayorAlDeVentaEnCualquierOrden() {
        Orden compra = crearOrden(
                "instrumento-1", LadoOrden.COMPRA, "100"
        );
        Orden venta = crearOrden(
                "instrumento-1", LadoOrden.VENTA, "90"
        );

        assertTrue(servicio.sonCompatibles(compra, venta));
        assertTrue(servicio.sonCompatibles(venta, compra));
    }

    @Test
    void aceptaPreciosIguales() {
        Orden compra = crearOrden(
                "instrumento-1", LadoOrden.COMPRA, "100"
        );
        Orden venta = crearOrden(
                "instrumento-1", LadoOrden.VENTA, "100"
        );

        assertTrue(servicio.sonCompatibles(compra, venta));
    }

    @Test
    void rechazaCompraConPrecioMenorAlDeVenta() {
        Orden compra = crearOrden(
                "instrumento-1", LadoOrden.COMPRA, "80"
        );
        Orden venta = crearOrden(
                "instrumento-1", LadoOrden.VENTA, "90"
        );

        assertFalse(servicio.sonCompatibles(compra, venta));
    }

    @Test
    void rechazaInstrumentosDiferentes() {
        Orden compra = crearOrden(
                "instrumento-1", LadoOrden.COMPRA, "100"
        );
        Orden venta = crearOrden(
                "instrumento-2", LadoOrden.VENTA, "90"
        );

        assertFalse(servicio.sonCompatibles(compra, venta));
    }

    @Test
    void rechazaDosComprasODosVentas() {
        for (LadoOrden lado : LadoOrden.values()) {
            Orden primera = crearOrden("instrumento-1", lado, "100");
            Orden segunda = crearOrden("instrumento-1", lado, "90");

            assertFalse(servicio.sonCompatibles(primera, segunda));
        }
    }

    @Test
    void rechazaCompararUnaOrdenConsigoMisma() {
        Orden orden = crearOrden(
                "instrumento-1", LadoOrden.COMPRA, "100"
        );

        assertFalse(servicio.sonCompatibles(orden, orden));
    }

    @Test
    void rechazaUnaOrdenCancelada() {
        Orden compra = crearOrden(
                "instrumento-1", LadoOrden.COMPRA, "100"
        );
        Orden venta = crearOrden(
                "instrumento-1", LadoOrden.VENTA, "90"
        );

        venta.cancelar();

        assertFalse(servicio.sonCompatibles(compra, venta));
        assertFalse(servicio.sonCompatibles(venta, compra));
    }

    @Test
    void rechazaUnaOrdenEjecutada() {
        Orden compra = crearOrden(
                "instrumento-1", LadoOrden.COMPRA, "100"
        );
        Orden venta = crearOrden(
                "instrumento-1", LadoOrden.VENTA, "90"
        );

        compra.ejecutar();

        assertFalse(servicio.sonCompatibles(compra, venta));
        assertFalse(servicio.sonCompatibles(venta, compra));
    }

    @Test
    void compararNoEjecutaLasOrdenes() {
        Orden compra = crearOrden(
                "instrumento-1", LadoOrden.COMPRA, "100"
        );
        Orden venta = crearOrden(
                "instrumento-1", LadoOrden.VENTA, "90"
        );

        assertTrue(servicio.sonCompatibles(compra, venta));

        assertAll(
                () -> assertTrue(compra.estaPendiente()),
                () -> assertTrue(venta.estaPendiente())
        );
    }

    @Test
    void rechazaArgumentosNulos() {
        Orden orden = crearOrden(
                "instrumento-1", LadoOrden.COMPRA, "100"
        );

        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> servicio.sonCompatibles(null, orden)
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> servicio.sonCompatibles(orden, null)
                )
        );
    }

    @Test
    void emparejaOrdenesLimitesYGeneraTransaccion() {
        Orden compra = crearOrden(
                "instrumento-1", LadoOrden.COMPRA, "100"
        );
        Orden venta = crearOrden(
                "instrumento-1", LadoOrden.VENTA, "90"
        );

        Transaccion tx = servicio.emparejar(compra, venta, null);

        assertAll(
                () -> assertEquals(EstadoOrden.EJECUTADA, compra.estado()),
                () -> assertEquals(EstadoOrden.EJECUTADA, venta.estado()),
                () -> assertEquals(compra.id(), tx.ordenCompraId()),
                () -> assertEquals(venta.id(), tx.ordenVentaId()),
                () -> assertEquals(0, new BigDecimal("90").compareTo(tx.precioEjecucion())),
                () -> assertEquals(0, new BigDecimal("5").compareTo(tx.cantidad())),
                () -> assertNotNull(tx.ejecutadaEn())
        );
    }

    @Test
    void emparejaOrdenMercadoContraOrdenLimite() {
        Orden compraMercado = OrdenFactory.crearMercado(
                "comprador-1",
                "instrumento-1",
                LadoOrden.COMPRA,
                new BigDecimal("5")
        );
        Orden ventaLimite = crearOrden(
                "instrumento-1", LadoOrden.VENTA, "95"
        );

        assertTrue(servicio.sonCompatibles(compraMercado, ventaLimite));

        Transaccion tx = servicio.emparejar(compraMercado, ventaLimite, null);
        assertEquals(0, new BigDecimal("95").compareTo(tx.precioEjecucion()));
    }
}