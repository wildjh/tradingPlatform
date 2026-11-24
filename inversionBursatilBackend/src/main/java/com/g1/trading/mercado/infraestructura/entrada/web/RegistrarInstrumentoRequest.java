package com.g1.trading.mercado.infraestructura.entrada.web;

import com.g1.trading.mercado.dominio.TipoInstrumento;

import java.math.BigDecimal;

/** Cuerpo JSON para registrar un instrumento en el catálogo. */
public record RegistrarInstrumentoRequest(
        String simbolo,
        String nombre,
        TipoInstrumento tipo,
        BigDecimal precioInicial,
        String generadoPor) {
}
