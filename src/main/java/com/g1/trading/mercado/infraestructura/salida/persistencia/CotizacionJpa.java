package com.g1.trading.mercado.infraestructura.salida.persistencia;

import com.g1.trading.mercado.dominio.OrigenCotizacion;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.math.BigDecimal;
import java.time.Instant;

/** Fila embebida de una cotización. Solo la ve el adaptador de salida. */
@Embeddable
public class CotizacionJpa {

    private BigDecimal valor;
    private Instant momento;

    @Enumerated(EnumType.STRING)
    private OrigenCotizacion origen;

    private String generadoPor;

    protected CotizacionJpa() {
    }

    public CotizacionJpa(BigDecimal valor, Instant momento, OrigenCotizacion origen, String generadoPor) {
        this.valor = valor;
        this.momento = momento;
        this.origen = origen;
        this.generadoPor = generadoPor;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public Instant getMomento() {
        return momento;
    }

    public OrigenCotizacion getOrigen() {
        return origen;
    }

    public String getGeneradoPor() {
        return generadoPor;
    }
}
