package com.g1.trading.mercado.infraestructura.entrada.web;

import com.g1.trading.mercado.dominio.Cotizacion;
import com.g1.trading.mercado.dominio.HistoricoPrecios;
import com.g1.trading.mercado.dominio.Instrumento;

import java.util.List;

/** Traduce el agregado al JSON que ve el cliente. El núcleo no conoce estos records. */
public final class InstrumentoMapper {

    private InstrumentoMapper() {
    }

    public static InstrumentoResponse aRespuesta(Instrumento instrumento) {
        return new InstrumentoResponse(
                instrumento.id().valor(),
                instrumento.nombre(),
                instrumento.tipo(),
                aRespuesta(instrumento.obtenerCotizacionActual()));
    }

    public static List<CotizacionResponse> aHistorico(HistoricoPrecios historico) {
        return historico.cotizaciones().stream().map(InstrumentoMapper::aRespuesta).toList();
    }

    private static CotizacionResponse aRespuesta(Cotizacion cotizacion) {
        return new CotizacionResponse(
                cotizacion.precio().valor(),
                cotizacion.precio().momento(),
                cotizacion.origen(),
                cotizacion.generadoPor());
    }
}
