package com.g1.ordenes.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Transaccion {
    private static final int DECIMALES = 4;

    private final String id;
    private final String ordenCompraId;
    private final String ordenVentaId;
    private final String compradorId;
    private final String vendedorId;
    private final String instrumentoId;
    private final BigDecimal cantidad;
    private final BigDecimal precioEjecucion;
    private final Instant ejecutadaEn;

    private Transaccion(String id, String ordenCompraId, String ordenVentaId, String compradorId,
                        String vendedorId, String instrumentoId, BigDecimal cantidad, BigDecimal precioEjecucion,
                        Instant ejecutadaEn) {
        this.id = Objects.requireNonNull(id);
        this.ordenCompraId = Objects.requireNonNull(ordenCompraId);
        this.ordenVentaId = Objects.requireNonNull(ordenVentaId);
        this.compradorId = Objects.requireNonNull(compradorId);
        this.vendedorId = Objects.requireNonNull(vendedorId);
        this.instrumentoId = Objects.requireNonNull(instrumentoId);
        this.cantidad = cantidad.stripTrailingZeros();
        this.precioEjecucion = precioEjecucion.setScale(DECIMALES, RoundingMode.HALF_UP);
        this.ejecutadaEn = Objects.requireNonNull(ejecutadaEn);
    }

    static Transaccion registrar(Orden compra, Orden venta, BigDecimal cantidadEjecutada, BigDecimal precioEjecucion) {
        if (compra == null || venta == null) throw new IllegalArgumentException("Se requieren ambas órdenes");
        if (compra.lado() != LadoOrden.COMPRA || venta.lado() != LadoOrden.VENTA) throw new IllegalArgumentException("Se requiere COMPRA y VENTA");
        if (cantidadEjecutada == null || cantidadEjecutada.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Cantidad inválida");
        if (precioEjecucion == null || precioEjecucion.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Precio inválido");

        return new Transaccion(UUID.randomUUID().toString(), compra.id(), venta.id(), compra.inversionistaId(),
                venta.inversionistaId(), compra.instrumentoId(), cantidadEjecutada, precioEjecucion, Instant.now());
    }

    public String id() { return id; }
    public String ordenCompraId() { return ordenCompraId; }
    public String ordenVentaId() { return ordenVentaId; }
    public String compradorId() { return compradorId; }
    public String vendedorId() { return vendedorId; }
    public String instrumentoId() { return instrumentoId; }
    public BigDecimal cantidad() { return cantidad; }
    public BigDecimal precioEjecucion() { return precioEjecucion; }
    public Instant ejecutadaEn() { return ejecutadaEn; }
}
