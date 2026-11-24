package com.g1.trading.ordenes;

import java.math.BigDecimal;

/**
 * Servicio de dominio que evalúa compatibilidad y ejecuta el cruce
 * entre órdenes de compra y venta (HU-05).
 */
public final class EmparejamientoService {

    public boolean sonCompatibles(Orden primera, Orden segunda) {
        if (primera == null || segunda == null) {
            throw new IllegalArgumentException(
                    "Se necesitan dos órdenes para comparar"
            );
        }

        if (primera.equals(segunda)) {
            return false;
        }

        if (!primera.estaPendiente() || !segunda.estaPendiente()) {
            return false;
        }

        if (!primera.instrumentoId().equals(segunda.instrumentoId())) {
            return false;
        }

        if (primera.lado() == segunda.lado()) {
            return false;
        }

        if (primera.cantidad().compareTo(segunda.cantidad()) != 0) {
            return false;
        }

        // Si alguna orden es a MERCADO, acepta el precio de la contraparte o del mercado
        if (primera.tipo() == TipoOrden.MERCADO || segunda.tipo() == TipoOrden.MERCADO) {
            return true;
        }

        Orden compra = primera.lado() == LadoOrden.COMPRA ? primera : segunda;
        Orden venta = primera.lado() == LadoOrden.VENTA ? primera : segunda;

        return compra.precioLimite().valor()
                .compareTo(venta.precioLimite().valor()) >= 0;
    }

    /**
     * Ejecuta el emparejamiento de dos órdenes compatibles, cambia su estado
     * a EJECUTADA y genera el registro de Transaccion correspondiente.
     */
    public Transaccion emparejar(
            Orden primera,
            Orden segunda,
            BigDecimal precioMercadoActual
    ) {
        if (!sonCompatibles(primera, segunda)) {
            throw new IllegalStateException(
                    "Las órdenes no son compatibles para emparejarse"
            );
        }

        Orden compra = primera.lado() == LadoOrden.COMPRA ? primera : segunda;
        Orden venta = primera.lado() == LadoOrden.VENTA ? primera : segunda;

        BigDecimal precioEjecucion = resolverPrecioEjecucion(
                compra, venta, precioMercadoActual
        );

        compra.ejecutar();
        venta.ejecutar();

        return Transaccion.registrar(
                compra,
                venta,
                compra.cantidad(),
                precioEjecucion
        );
    }

    private BigDecimal resolverPrecioEjecucion(
            Orden compra,
            Orden venta,
            BigDecimal precioMercadoActual
    ) {
        if (compra.tipo() == TipoOrden.LIMITE && venta.tipo() == TipoOrden.LIMITE) {
            return venta.precioLimite().valor();
        }
        if (compra.tipo() == TipoOrden.LIMITE) {
            return compra.precioLimite().valor();
        }
        if (venta.tipo() == TipoOrden.LIMITE) {
            return venta.precioLimite().valor();
        }
        if (precioMercadoActual == null || precioMercadoActual.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Se requiere un precio de mercado positivo para cruzar dos órdenes a mercado"
            );
        }
        return precioMercadoActual;
    }
}