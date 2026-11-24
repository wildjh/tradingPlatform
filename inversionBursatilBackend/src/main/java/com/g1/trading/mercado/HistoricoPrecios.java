package com.g1.trading.mercado;

import java.util.ArrayList;
import java.util.List;

/**
 * Serie de cotizaciones de un mismo instrumento, ordenada en el tiempo (HU-02).
 * Solo el agregado Instrumento puede agregar entradas.
 */
public final class HistoricoPrecios {

    private final List<Cotizacion> cotizaciones = new ArrayList<>();

    HistoricoPrecios(Cotizacion inicial) {
        if (inicial == null) {
            throw new IllegalArgumentException("El histórico nace con una cotización");
        }
        cotizaciones.add(inicial);
    }

    void registrar(Cotizacion cotizacion) {
        if (cotizacion == null) {
            throw new IllegalArgumentException("La cotización nueva no puede ser nula");
        }
        if (!cotizacion.precio().momento().isAfter(actual().precio().momento())) {
            throw new IllegalArgumentException(
                    "La nueva cotización debe ser posterior a " + actual().precio().momento());
        }
        cotizaciones.add(cotizacion);
    }

    public Cotizacion actual() {
        return cotizaciones.get(cotizaciones.size() - 1);
    }

    /** Copia: consultar el histórico no permite modificar el agregado. */
    public List<Cotizacion> cotizaciones() {
        return List.copyOf(cotizaciones);
    }
}
