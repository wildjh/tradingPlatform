package com.g1.trading.ordenes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Implementación en memoria del libro de órdenes respetando prioridad FIFO de llegada. */
public final class OrdenRepositoryEnMemoria implements OrdenRepository {

    private final Map<String, Orden> ordenes = new LinkedHashMap<>();

    @Override
    public synchronized void guardar(Orden orden) {
        if (orden == null) {
            throw new IllegalArgumentException("La orden no puede ser nula");
        }
        ordenes.put(orden.id(), orden);
    }

    @Override
    public synchronized Optional<Orden> buscarPorId(String ordenId) {
        if (ordenId == null || ordenId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(ordenes.get(ordenId.trim()));
    }

    @Override
    public synchronized List<Orden> buscarPendientesPorInstrumentoYLado(String instrumentoId, LadoOrden lado) {
        if (instrumentoId == null || instrumentoId.isBlank() || lado == null) {
            return List.of();
        }
        String simbolo = instrumentoId.trim().toUpperCase();
        List<Orden> resultado = new ArrayList<>();
        for (Orden orden : ordenes.values()) {
            if (orden.estaPendiente()
                    && orden.instrumentoId().equals(simbolo)
                    && orden.lado() == lado) {
                resultado.add(orden);
            }
        }
        return List.copyOf(resultado);
    }

    @Override
    public synchronized List<Orden> buscarPorInversionista(String inversionistaId) {
        if (inversionistaId == null || inversionistaId.isBlank()) {
            return List.of();
        }
        String id = inversionistaId.trim();
        return ordenes.values().stream()
                .filter(orden -> orden.inversionistaId().equals(id))
                .toList();
    }
}
