package com.g1.mercado.dominio;

import java.util.ArrayList;
import java.util.List;

public final class HistoricoPrecios {
    private final List<Cotizacion> cotizaciones = new ArrayList<>();

    HistoricoPrecios(Cotizacion inicial) {
        if (inicial == null) throw new IllegalArgumentException("El histórico nace con una cotización");
        cotizaciones.add(inicial);
    }

    void registrar(Cotizacion cotizacion) {
        if (cotizacion == null) throw new IllegalArgumentException("La cotización nueva no puede ser nula");
        if (!cotizacion.precio().momento().isAfter(actual().precio().momento())) {
            throw new IllegalArgumentException("La nueva cotización debe ser posterior a " + actual().precio().momento());
        }
        cotizaciones.add(cotizacion);
    }

    public Cotizacion actual() { return cotizaciones.get(cotizaciones.size() - 1); }
    public List<Cotizacion> cotizaciones() { return List.copyOf(cotizaciones); }
}
