package com.g1.trading.ordenes;

import com.g1.trading.portafolio.PortafolioService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Entrada pública del contexto Órdenes y emparejamiento (HU-03, HU-04, HU-05, HU-07).
 * Coordina la validación de venta y actualización de tenencias con PortafolioService.
 */
public final class OrdenService {

    private final OrdenRepository ordenRepository;
    private final TransaccionRepository transaccionRepository;
    private final EmparejamientoService emparejamientoService;
    private final PortafolioService portafolioService;

    public OrdenService(
            OrdenRepository ordenRepository,
            TransaccionRepository transaccionRepository,
            EmparejamientoService emparejamientoService,
            PortafolioService portafolioService
    ) {
        if (ordenRepository == null) {
            throw new IllegalArgumentException("Se necesita un repositorio de órdenes");
        }
        if (transaccionRepository == null) {
            throw new IllegalArgumentException("Se necesita un repositorio de transacciones");
        }
        if (emparejamientoService == null) {
            throw new IllegalArgumentException("Se necesita el servicio de emparejamiento");
        }
        if (portafolioService == null) {
            throw new IllegalArgumentException("Se necesita el servicio de portafolio");
        }
        this.ordenRepository = ordenRepository;
        this.transaccionRepository = transaccionRepository;
        this.emparejamientoService = emparejamientoService;
        this.portafolioService = portafolioService;
    }

    public record ResultadoOrden(Orden orden, Transaccion transaccion) {
        public boolean fueEjecutada() {
            return transaccion != null;
        }
    }

    public synchronized ResultadoOrden crearOrdenLimite(
            String inversionistaId,
            String instrumentoId,
            LadoOrden lado,
            BigDecimal cantidad,
            BigDecimal precioLimite
    ) {
        String simbolo = normalizarSimbolo(instrumentoId);
        validarVentaSiAplica(inversionistaId, simbolo, lado, cantidad);
        Orden nueva = OrdenFactory.crearLimite(inversionistaId, simbolo, lado, cantidad, precioLimite);
        return procesarOrden(nueva, null);
    }

    public synchronized ResultadoOrden crearOrdenMercado(
            String inversionistaId,
            String instrumentoId,
            LadoOrden lado,
            BigDecimal cantidad,
            BigDecimal precioMercadoReferencia
    ) {
        String simbolo = normalizarSimbolo(instrumentoId);
        validarVentaSiAplica(inversionistaId, simbolo, lado, cantidad);
        Orden nueva = OrdenFactory.crearMercado(inversionistaId, simbolo, lado, cantidad);
        return procesarOrden(nueva, precioMercadoReferencia);
    }

    private ResultadoOrden procesarOrden(Orden nuevaOrden, BigDecimal precioMercadoReferencia) {
        LadoOrden ladoOpuesto = (nuevaOrden.lado() == LadoOrden.COMPRA)
                ? LadoOrden.VENTA
                : LadoOrden.COMPRA;

        List<Orden> candidatas = ordenRepository.buscarPendientesPorInstrumentoYLado(
                nuevaOrden.instrumentoId(),
                ladoOpuesto
        );

        for (Orden candidata : candidatas) {
            if (emparejamientoService.sonCompatibles(nuevaOrden, candidata)) {
                Transaccion tx = emparejamientoService.emparejar(
                        nuevaOrden,
                        candidata,
                        precioMercadoReferencia
                );

                portafolioService.aplicarTransaccion(
                        tx.compradorId(),
                        tx.vendedorId(),
                        tx.instrumentoId(),
                        tx.cantidad(),
                        tx.precioEjecucion()
                );

                ordenRepository.guardar(nuevaOrden);
                ordenRepository.guardar(candidata);
                transaccionRepository.guardar(tx);

                return new ResultadoOrden(nuevaOrden, tx);
            }
        }

        ordenRepository.guardar(nuevaOrden);
        return new ResultadoOrden(nuevaOrden, null);
    }

    public synchronized Orden cancelarOrden(String ordenId, String inversionistaId) {
        if (ordenId == null || ordenId.isBlank()) {
            throw new IllegalArgumentException("Se necesita el id de la orden");
        }
        if (inversionistaId == null || inversionistaId.isBlank()) {
            throw new IllegalArgumentException("Se necesita el id del inversionista");
        }

        Orden orden = ordenRepository.buscarPorId(ordenId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada: " + ordenId));

        if (!orden.inversionistaId().equals(inversionistaId.trim())) {
            throw new IllegalStateException("La orden no pertenece al inversionista indicado");
        }

        orden.cancelar();
        ordenRepository.guardar(orden);
        return orden;
    }

    public synchronized Optional<Orden> buscarOrden(String ordenId) {
        return ordenRepository.buscarPorId(ordenId);
    }

    public synchronized List<Orden> listarOrdenesPorInversionista(String inversionistaId) {
        return ordenRepository.buscarPorInversionista(inversionistaId);
    }

    public synchronized List<Orden> listarLibroPendiente(String instrumentoId, LadoOrden lado) {
        return ordenRepository.buscarPendientesPorInstrumentoYLado(normalizarSimbolo(instrumentoId), lado);
    }

    public synchronized List<Transaccion> listarTransaccionesPorInversionista(String inversionistaId) {
        return transaccionRepository.buscarPorInversionista(inversionistaId);
    }

    private void validarVentaSiAplica(
            String inversionistaId,
            String simbolo,
            LadoOrden lado,
            BigDecimal cantidad
    ) {
        if (lado == LadoOrden.VENTA) {
            portafolioService.validarVenta(inversionistaId, simbolo, cantidad);
        }
    }

    private static String normalizarSimbolo(String instrumentoId) {
        if (instrumentoId == null || instrumentoId.isBlank()) {
            throw new IllegalArgumentException("Se necesita el id del instrumento");
        }
        return instrumentoId.trim().toUpperCase();
    }
}
