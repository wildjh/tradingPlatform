package com.g1.trading.ordenes;

/**
 * Servicio de dominio que compara órdenes con precio límite.
 *
 * Evalúa compatibilidad; no ejecuta órdenes ni actualiza portafolios.
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

        if (primera.tipo() != TipoOrden.LIMITE
                || segunda.tipo() != TipoOrden.LIMITE) {
            throw new UnsupportedOperationException(
                    "El emparejamiento a mercado aún no está definido"
            );
        }

        Orden compra = primera.lado() == LadoOrden.COMPRA
                ? primera : segunda;

        Orden venta = primera.lado() == LadoOrden.VENTA
                ? primera : segunda;

        return compra.precioLimite().valor()
                .compareTo(venta.precioLimite().valor()) >= 0;
    }
}