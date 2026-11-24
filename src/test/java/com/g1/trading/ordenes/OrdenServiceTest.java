package com.g1.trading.ordenes;

import com.g1.trading.portafolio.PortafolioRepositoryEnMemoria;
import com.g1.trading.portafolio.PortafolioService;
import com.g1.trading.portafolio.VentaNoPermitidaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrdenServiceTest {

    private PortafolioService portafolioService;
    private OrdenService ordenService;

    @BeforeEach
    void setUp() {
        portafolioService = new PortafolioService(new PortafolioRepositoryEnMemoria());
        ordenService = new OrdenService(
                new OrdenRepositoryEnMemoria(),
                new TransaccionRepositoryEnMemoria(),
                new EmparejamientoService(),
                portafolioService
        );
    }

    @Test
    void rechazaOrdenDeVentaSiElInversionistaNoTieneTenenciaSuficiente() {
        assertThrows(
                VentaNoPermitidaException.class,
                () -> ordenService.crearOrdenLimite(
                        "vendedor-sin-saldo",
                        "AAPL",
                        LadoOrden.VENTA,
                        new BigDecimal("10"),
                        new BigDecimal("150")
                )
        );
    }

    @Test
    void dejaOrdenPendienteCuandoNoExisteContraparteCompatible() {
        var resultado = ordenService.crearOrdenLimite(
                "comprador-1",
                "AAPL",
                LadoOrden.COMPRA,
                new BigDecimal("5"),
                new BigDecimal("140")
        );

        assertFalse(resultado.fueEjecutada());
        assertNull(resultado.transaccion());
        assertEquals(EstadoOrden.PENDIENTE, resultado.orden().estado());
        assertEquals(1, ordenService.listarLibroPendiente("AAPL", LadoOrden.COMPRA).size());
    }

    @Test
    void emparejaOrdenesLimitesEnFifoYActualizaPortafolios() {
        portafolioService.cargarPosicionInicial(
                "vendedor-1", "AAPL", new BigDecimal("10"), new BigDecimal("120")
        );

        var venta = ordenService.crearOrdenLimite(
                "vendedor-1",
                "AAPL",
                LadoOrden.VENTA,
                new BigDecimal("10"),
                new BigDecimal("150")
        );
        assertFalse(venta.fueEjecutada());

        var compra = ordenService.crearOrdenLimite(
                "comprador-1",
                "AAPL",
                LadoOrden.COMPRA,
                new BigDecimal("10"),
                new BigDecimal("155")
        );

        assertTrue(compra.fueEjecutada());
        Transaccion tx = compra.transaccion();
        assertNotNull(tx);
        assertEquals(0, new BigDecimal("150").compareTo(tx.precioEjecucion()));
        assertEquals(EstadoOrden.EJECUTADA, compra.orden().estado());
        assertEquals(EstadoOrden.EJECUTADA, venta.orden().estado());
        assertTrue(ordenService.listarLibroPendiente("AAPL", LadoOrden.VENTA).isEmpty());

        // El comprador ahora posee las 10 unidades y el vendedor ya no puede volver a venderlas
        assertDoesNotThrow(() -> portafolioService.validarVenta("comprador-1", "AAPL", new BigDecimal("10")));
        assertThrows(
                VentaNoPermitidaException.class,
                () -> portafolioService.validarVenta("vendedor-1", "AAPL", new BigDecimal("1"))
        );

        List<Transaccion> historial = ordenService.listarTransaccionesPorInversionista("comprador-1");
        assertEquals(1, historial.size());
    }

    @Test
    void emparejaOrdenMercadoContraOrdenLimitePendiente() {
        portafolioService.cargarPosicionInicial(
                "vendedor-1", "MSFT", new BigDecimal("5"), new BigDecimal("300")
        );

        ordenService.crearOrdenLimite(
                "vendedor-1",
                "MSFT",
                LadoOrden.VENTA,
                new BigDecimal("5"),
                new BigDecimal("320")
        );

        var compraMercado = ordenService.crearOrdenMercado(
                "comprador-1",
                "MSFT",
                LadoOrden.COMPRA,
                new BigDecimal("5"),
                new BigDecimal("325")
        );

        assertTrue(compraMercado.fueEjecutada());
        assertEquals(0, new BigDecimal("320").compareTo(compraMercado.transaccion().precioEjecucion()));
    }

    @Test
    void permiteCancelarOrdenPendienteYSoloAlPropietario() {
        var creada = ordenService.crearOrdenLimite(
                "comprador-1",
                "TSLA",
                LadoOrden.COMPRA,
                new BigDecimal("3"),
                new BigDecimal("200")
        );

        assertThrows(
                IllegalStateException.class,
                () -> ordenService.cancelarOrden(creada.orden().id(), "otro-usuario")
        );

        Orden cancelada = ordenService.cancelarOrden(creada.orden().id(), "comprador-1");
        assertEquals(EstadoOrden.CANCELADA, cancelada.estado());
        assertTrue(ordenService.listarLibroPendiente("TSLA", LadoOrden.COMPRA).isEmpty());
    }
}
