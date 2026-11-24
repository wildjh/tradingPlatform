package com.g1.trading.mercado.aplicacion;

import com.g1.trading.mercado.dominio.HistoricoPrecios;
import com.g1.trading.mercado.dominio.Instrumento;
import com.g1.trading.mercado.dominio.TipoInstrumento;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Puerto primario de Mercado.
 * Dice qué puede pedirle el exterior al catálogo. No dice si eso llega por HTTP
 * ni dónde se guardan los instrumentos.
 */
public interface InstrumentoUseCase {

    /** HU-01: lista de instrumentos con su cotización vigente. */
    List<Instrumento> listarConCotizacionActual();

    /** HU-02: serie de precios de un símbolo. */
    HistoricoPrecios consultarHistorico(String simbolo);

    Instrumento registrar(String simbolo, String nombre, TipoInstrumento tipo,
                          BigDecimal precioInicial, Instant momento, String generadoPor);

    /** HU-08: mismo factor y mismo instante para todo el catálogo. */
    void simularPrecios(BigDecimal factorVariacion, Instant momento, String generadoPor);
}
