package com.g1.trading.ordenes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Registro inmutable de un emparejamiento ejecutado entre una orden de compra
 * y una orden de venta (HU-05 y HU-07).
 */
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

    private Transaccion(
            String id,  String ordenCompraId, String ordenVentaId, String compradorId,
            String vendedorId, String instrumentoId, BigDecimal cantidad, BigDecimal precioEjecucion,
            Instant ejecutadaEn
    ) {
        this.id = Objects.requireNonNull(id, "id requerido");
        this.ordenCompraId = Objects.requireNonNull(ordenCompraId, "ordenCompraId requerido");
        this.ordenVentaId = Objects.requireNonNull(ordenVentaId, "ordenVentaId requerido");
        this.compradorId = Objects.requireNonNull(compradorId, "compradorId requerido");
        this.vendedorId = Objects.requireNonNull(vendedorId, "vendedorId requerido");
        this.instrumentoId = Objects.requireNonNull(instrumentoId, "instrumentoId requerido");
        this.cantidad = cantidad.stripTrailingZeros();
        this.precioEjecucion = precioEjecucion.setScale(DECIMALES, RoundingMode.HALF_UP);
        this.ejecutadaEn = Objects.requireNonNull(ejecutadaEn, "ejecutadaEn requerido");
    }

    static Transaccion registrar(
            Orden compra,
            Orden venta,
            BigDecimal cantidadEjecutada,
            BigDecimal precioEjecucion
    ) {
        if (compra == null || venta == null) {
            throw new IllegalArgumentException("Se requieren ambas órdenes para registrar la transacción");
        }
        if (compra.lado() != LadoOrden.COMPRA || venta.lado() != LadoOrden.VENTA) {
            throw new IllegalArgumentException("La transacción requiere una orden de COMPRA y una de VENTA");
        }
        if (cantidadEjecutada == null || cantidadEjecutada.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad ejecutada debe ser mayor a cero");
        }
        if (precioEjecucion == null || precioEjecucion.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio de ejecución debe ser mayor a cero");
        }

        return new Transaccion(
                UUID.randomUUID().toString(),
                compra.id(),
                venta.id(),
                compra.inversionistaId(),
                venta.inversionistaId(),
                compra.instrumentoId(),
                cantidadEjecutada,
                precioEjecucion,
                Instant.now()
        );
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaccion that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}