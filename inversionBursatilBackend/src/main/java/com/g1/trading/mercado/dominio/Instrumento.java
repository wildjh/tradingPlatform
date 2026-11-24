package com.g1.trading.mercado.dominio;

import java.util.List;
import java.util.Objects;

/**
 * Raíz del agregado de Mercado.
 * <p>
 * Controla la cotización actual y el histórico de precios. Un instrumento
 * siempre tiene al menos un {@link PrecioCotizado} válido. La consulta
 * (HU-01 y HU-02) no necesita al motor de emparejamiento: no hay ninguna
 * referencia a órdenes ni a portafolio.
 * <p>
 * El constructor no es público. La aplicación crea instancias con
 * {@code crear} y la persistencia las recupera con {@code reconstituir}.
 */
public final class Instrumento {

    private final InstrumentoId id;
    private final String nombre;
    private final TipoInstrumento tipo;
    private final HistoricoPrecios historico;

    Instrumento(InstrumentoId id, String nombre, TipoInstrumento tipo, Cotizacion cotizacionInicial) {
        if (id == null) {
            throw new IllegalArgumentException("El instrumento necesita un id");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El instrumento necesita un nombre");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("El instrumento necesita un tipo (ACCION o ETF)");
        }
        if (cotizacionInicial == null) {
            throw new IllegalArgumentException("El instrumento nace con una cotización inicial");
        }
        this.id = id;
        this.nombre = nombre.trim();
        this.tipo = tipo;
        this.historico = new HistoricoPrecios(cotizacionInicial);
    }

    /** Alta de un instrumento nuevo. La usa la factory, que vive en aplicación. */
    public static Instrumento crear(InstrumentoId id, String nombre, TipoInstrumento tipo, Cotizacion inicial) {
        return new Instrumento(id, nombre, tipo, inicial);
    }

    /**
     * Reconstruye un instrumento ya guardado, con todo su histórico.
     * Lo usa el adaptador de persistencia, sin saltarse el orden temporal.
     */
    public static Instrumento reconstituir(InstrumentoId id, String nombre, TipoInstrumento tipo,
                                           List<Cotizacion> cotizaciones) {
        if (cotizaciones == null || cotizaciones.isEmpty()) {
            throw new IllegalArgumentException("El instrumento almacenado necesita al menos una cotización");
        }
        Instrumento instrumento = new Instrumento(id, nombre, tipo, cotizaciones.get(0));
        for (int i = 1; i < cotizaciones.size(); i++) {
            Cotizacion cotizacion = cotizaciones.get(i);
            instrumento.actualizarPrecio(cotizacion.precio(), cotizacion.origen(), cotizacion.generadoPor());
        }
        return instrumento;
    }

    /**
     * Registra un precio nuevo. El histórico solo crece y cada momento es posterior al anterior.
     *
     * @param nuevoPrecio precio ya validado (mayor a cero, con marca de tiempo)
     * @param origen      qué generó el cambio (carga o simulación)
     * @param generadoPor quién lo generó, como id de texto
     */
    public void actualizarPrecio(PrecioCotizado nuevoPrecio, OrigenCotizacion origen, String generadoPor) {
        historico.registrar(new Cotizacion(nuevoPrecio, origen, generadoPor));
    }

    public InstrumentoId id() {
        return id;
    }

    public String nombre() {
        return nombre;
    }

    public TipoInstrumento tipo() {
        return tipo;
    }

    /** HU-01: precio vigente, con su trazabilidad. */
    public Cotizacion obtenerCotizacionActual() {
        return historico.actual();
    }

    /** HU-02: serie completa. La lista interna no se puede reemplazar desde afuera. */
    public HistoricoPrecios consultarHistorico() {
        return historico;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof Instrumento instrumento)) {
            return false;
        }
        return id.equals(instrumento.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
