package com.g1.trading.portafolio;

import com.g1.trading.compartido.InversionistaId;

import java.util.Optional;

/** Puerto de salida del contexto Portafolio. */
public interface PortafolioRepository {

    Optional<Portafolio> buscarPorInversionista(InversionistaId inversionistaId);

    void guardar(Portafolio portafolio);
}
