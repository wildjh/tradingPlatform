package com.g1.ordenes.dominio;

import java.math.BigDecimal;
import java.util.UUID;

public final class OrdenFactory {
    private OrdenFactory() {}

    public static Orden crearLimite(String inversionistaId, String instrumentoId, LadoOrden lado, BigDecimal cantidad, BigDecimal precio) {
        return new Orden(UUID.randomUUID().toString(), inversionistaId, instrumentoId, lado, TipoOrden.LIMITE, cantidad, new PrecioLimite(precio));
    }

    public static Orden crearMercado(String inversionistaId, String instrumentoId, LadoOrden lado, BigDecimal cantidad) {
        return new Orden(UUID.randomUUID().toString(), inversionistaId, instrumentoId, lado, TipoOrden.MERCADO, cantidad, null);
    }
}
