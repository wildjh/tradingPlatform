package com.g1.trading.ordenes;

import java.util.ArrayList;
import java.util.List;

/** Implementación en memoria del historial de transacciones ejecutadas. */
public final class TransaccionRepositoryEnMemoria implements TransaccionRepository {

    private final List<Transaccion> transacciones = new ArrayList<>();

    @Override
    public synchronized void guardar(Transaccion transaccion) {
        if (transaccion == null) {
            throw new IllegalArgumentException("La transacción no puede ser nula");
        }
        transacciones.add(transaccion);
    }

    @Override
    public synchronized List<Transaccion> buscarPorInversionista(String inversionistaId) {
        if (inversionistaId == null || inversionistaId.isBlank()) {
            return List.of();
        }
        String id = inversionistaId.trim();
        return transacciones.stream()
                .filter(tx -> tx.compradorId().equals(id) || tx.vendedorId().equals(id))
                .toList();
    }

    @Override
    public synchronized List<Transaccion> listarTodas() {
        return List.copyOf(transacciones);
    }
}
