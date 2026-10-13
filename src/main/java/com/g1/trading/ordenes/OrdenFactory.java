package com.g1.trading.ordenes;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Factory del agregado Orden.
 *
 * Construye órdenes a mercado o con precio límite.
 * Las validaciones internas las garantiza el agregado.
 *
 * La disponibilidad del portafolio debe validarse
 * antes de aceptar una venta.
 */
public final class OrdenFactory {

    private OrdenFactory() {
    }

    public static Orden crearLimite(
            String inversionistaId,
            String instrumentoId,
            LadoOrden lado,
            BigDecimal cantidad,
            BigDecimal precio
    ) {
        return new Orden(
                UUID.randomUUID().toString(),
                inversionistaId,
                instrumentoId,
                lado,
                TipoOrden.LIMITE,
                cantidad,
                new PrecioLimite(precio)
        );
    }

    public static Orden crearMercado(
            String inversionistaId,
            String instrumentoId,
            LadoOrden lado,
            BigDecimal cantidad
    ) {
        return new Orden(
                UUID.randomUUID().toString(),
                inversionistaId,
                instrumentoId,
                lado,
                TipoOrden.MERCADO,
                cantidad,
                null
        );
    }
}