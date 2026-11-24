package com.g1.trading.mercado.infraestructura.entrada.web;

import java.math.BigDecimal;

/** Cuerpo JSON de la simulación periódica de precios (HU-08). */
public record SimularPreciosRequest(BigDecimal factorVariacion, String generadoPor) {
}
