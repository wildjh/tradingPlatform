package com.g1.mercado.dominio;

import java.util.Objects;

public final class Instrumento {
    private final InstrumentoId id;
    private final String nombre;
    private final TipoInstrumento tipo;
    private final HistoricoPrecios historico;

    Instrumento(InstrumentoId id, String nombre, TipoInstrumento tipo, Cotizacion cotizacionInicial) {
        if (id == null) throw new IllegalArgumentException("El instrumento necesita un id");
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El instrumento necesita un nombre");
        if (tipo == null) throw new IllegalArgumentException("El instrumento necesita un tipo (ACCION o ETF)");
        if (cotizacionInicial == null) throw new IllegalArgumentException("El instrumento nace con una cotización inicial");
        this.id = id;
        this.nombre = nombre.trim();
        this.tipo = tipo;
        this.historico = new HistoricoPrecios(cotizacionInicial);
    }

    public void actualizarPrecio(PrecioCotizado nuevoPrecio, OrigenCotizacion origen, String generadoPor) {
        historico.registrar(new Cotizacion(nuevoPrecio, origen, generadoPor));
    }

    public InstrumentoId id() { return id; }
    public String nombre() { return nombre; }
    public TipoInstrumento tipo() { return tipo; }
    public Cotizacion obtenerCotizacionActual() { return historico.actual(); }
    public HistoricoPrecios consultarHistorico() { return historico; }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) return true;
        if (!(otro instanceof Instrumento instrumento)) return false;
        return id.equals(instrumento.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
