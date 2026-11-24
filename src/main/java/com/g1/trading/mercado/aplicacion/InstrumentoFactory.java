package com.g1.trading.mercado.aplicacion;

import com.g1.trading.mercado.dominio.Cotizacion;
import com.g1.trading.mercado.dominio.Instrumento;
import com.g1.trading.mercado.dominio.InstrumentoId;
import com.g1.trading.mercado.dominio.OrigenCotizacion;
import com.g1.trading.mercado.dominio.PrecioCotizado;
import com.g1.trading.mercado.dominio.TipoInstrumento;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Caso de creación del agregado. Pertenece al núcleo (aplicación):
 * no importa Spring Data ni HTTP. Spring solo lo descubre para inyectarlo.
 */
@Component
public class InstrumentoFactory {

    public Instrumento registrar(String simbolo, String nombre, TipoInstrumento tipo,
                                 BigDecimal precioInicial, Instant momento, String generadoPor) {
        InstrumentoId id = new InstrumentoId(simbolo);
        String nombreLimpio = exigirNombre(nombre);
        if (tipo == null) {
            throw new IllegalArgumentException("El instrumento necesita un tipo (ACCION o ETF)");
        }
        if (generadoPor == null || generadoPor.isBlank()) {
            throw new IllegalArgumentException("La carga inicial debe indicar quién la generó");
        }
        PrecioCotizado precio = new PrecioCotizado(precioInicial, momento);
        Cotizacion inicial = new Cotizacion(precio, OrigenCotizacion.CARGA_INICIAL, generadoPor);
        return Instrumento.crear(id, nombreLimpio, tipo, inicial);
    }

    private static String exigirNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El instrumento necesita un nombre");
        }
        return nombre.trim();
    }
}
