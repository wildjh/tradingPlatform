package com.g1.trading.portafolio;



import java.math.BigDecimal;
import java.math.RoundingMode;

import com.g1.trading.mercado.InstrumentoId;

/**
 * Entidad interna del agregado Portafolio: tenencia de un instrumento.
 * Los métodos que cambian el estado son de acceso de paquete: solo la raíz los usa.
 */
public final class Posicion {

    private static final int DECIMALES = 4;

    private final InstrumentoId instrumentoId;
    private Cantidad cantidad;
    private BigDecimal costoPromedio;

    Posicion(InstrumentoId instrumentoId, Cantidad cantidad, BigDecimal costoPromedio) {
        if (instrumentoId == null) {
            throw new IllegalArgumentException("La posición necesita un instrumento");
        }
        this.instrumentoId = instrumentoId;
        this.cantidad = cantidad;
        this.costoPromedio = costoPromedio.setScale(DECIMALES, RoundingMode.HALF_UP);
    }

    void agregarCompra(Cantidad comprada, BigDecimal precio) {
        Cantidad total = cantidad.sumar(comprada);
        BigDecimal costoViejo = costoPromedio.multiply(cantidad.valor());
        BigDecimal costoNuevo = precio.multiply(comprada.valor());
        this.costoPromedio = costoViejo.add(costoNuevo).divide(total.valor(), DECIMALES, RoundingMode.HALF_UP);
        this.cantidad = total;
    }

    void reducir(Cantidad vendida) {
        this.cantidad = cantidad.restar(vendida);
    }

    public BigDecimal valorActual(BigDecimal precioActual) {
        return precioActual.multiply(cantidad.valor());
    }

    /** (precio actual - costo promedio) * cantidad. */
    public BigDecimal gananciaNoRealizada(BigDecimal precioActual) {
        return precioActual.subtract(costoPromedio).multiply(cantidad.valor());
    }

    public InstrumentoId instrumentoId() {
        return instrumentoId;
    }

    public Cantidad cantidad() {
        return cantidad;
    }

    public BigDecimal costoPromedio() {
        return costoPromedio;
    }
}
