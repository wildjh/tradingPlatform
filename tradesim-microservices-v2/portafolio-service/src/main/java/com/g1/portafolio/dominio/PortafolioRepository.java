package com.g1.portafolio.dominio;

import java.util.Optional;

public interface PortafolioRepository {
    Optional<Portafolio> buscarPorInversionista(InversionistaId inversionistaId);
    void guardar(Portafolio portafolio);
}
