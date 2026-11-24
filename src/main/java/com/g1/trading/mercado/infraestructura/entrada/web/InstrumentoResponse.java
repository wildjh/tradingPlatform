package com.g1.trading.mercado.infraestructura.entrada.web;

import com.g1.trading.mercado.dominio.TipoInstrumento;

public record InstrumentoResponse(
        String simbolo,
        String nombre,
        TipoInstrumento tipo,
        CotizacionResponse cotizacionActual) {
}
