package com.g1.trading.portafolio;

import com.g1.trading.compartido.InversionistaId;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Adaptador en memoria (aún no hay JPA). Se reemplaza sin tocar el dominio. */
public final class PortafolioRepositoryEnMemoria implements PortafolioRepository {

    private final Map<InversionistaId, Portafolio> almacen = new ConcurrentHashMap<>();

    @Override
    public Optional<Portafolio> buscarPorInversionista(InversionistaId inversionistaId) {
        return Optional.ofNullable(almacen.get(inversionistaId));
    }

    @Override
    public void guardar(Portafolio portafolio) {
        almacen.put(portafolio.inversionistaId(), portafolio);
    }
}
