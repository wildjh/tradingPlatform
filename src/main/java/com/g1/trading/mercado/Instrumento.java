package com.g1.trading.mercado;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Paso 4 · Raíz del agregado Instrumento.
 * <p>
 * El agregado es el instrumento junto con su cotización actual y su histórico.
 * Quien quiera leer o cambiar precios pasa por esta raíz: el histórico no se
 * edita desde afuera.
 * <p>
 * Identidad publicada a otros contextos: {@link #simbolo()}, un String.
 * Esta clase no guarda objetos de Orden ni de Portafolio.
 * <p>
 * La construcción pública está en {@link InstrumentoFactory}. El constructor
 * queda visible solo en el paquete para que la raíz siga rechazando un estado
 * imposible aunque alguien la arme a mano.
 */
public class Instrumento {

    private final String simbolo;
    private final String nombre;
    private final TipoInstrumento tipo;
    private PrecioCotizado cotizacionActual;
    private final List<PrecioCotizado> historico = new ArrayList<>();

    Instrumento(String simbolo, String nombre, TipoInstrumento tipo, PrecioCotizado cotizacionInicial) {
        if (simbolo == null || !simbolo.matches("[A-Z]{1,5}")) {
            throw new IllegalArgumentException("El símbolo debe tener de 1 a 5 letras mayúsculas");
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
        this.simbolo = simbolo;
        this.nombre = nombre.trim();
        this.tipo = tipo;
        this.cotizacionActual = cotizacionInicial;
        this.historico.add(cotizacionInicial);
    }

    /**
     * Evento de dominio: precio cotizado actualizado.
     * El histórico solo crece, y cada marca de tiempo es posterior a la anterior.
     */
    public void registrarCotizacion(PrecioCotizado nueva) {
        if (nueva == null) {
            throw new IllegalArgumentException("La cotización nueva no puede ser nula");
        }
        if (!nueva.momento().isAfter(cotizacionActual.momento())) {
            throw new IllegalArgumentException(
                    "La nueva cotización de " + simbolo + " debe ser posterior a " + cotizacionActual.momento());
        }
        this.cotizacionActual = nueva;
        this.historico.add(nueva);
    }

    /** Id que Órdenes y Portafolio pueden guardar. No es el objeto completo. */
    public String simbolo() {
        return simbolo;
    }

    public String nombre() {
        return nombre;
    }

    public TipoInstrumento tipo() {
        return tipo;
    }

    public PrecioCotizado cotizacionActual() {
        return cotizacionActual;
    }

    /** Copia defensiva: HU-02 consulta el histórico sin poder alterarlo. */
    public List<PrecioCotizado> historico() {
        return List.copyOf(historico);
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof Instrumento instrumento)) {
            return false;
        }
        return simbolo.equals(instrumento.simbolo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(simbolo);
    }
}
