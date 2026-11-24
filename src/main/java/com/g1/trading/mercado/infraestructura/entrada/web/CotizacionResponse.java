package com.g1.trading.mercado.infraestructura.entrada.web;

import com.g1.trading.mercado.dominio.OrigenCotizacion;

import java.math.BigDecimal;
import java.time.Instant;

public record CotizacionResponse(
        BigDecimal valor,
        Instant momento,
        OrigenCotizacion origen,
        String generadoPor) {
}
