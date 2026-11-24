package com.g1.portafolio.dominio;

import java.math.BigDecimal;
import java.util.List;

public record ResumenPortafolio(InversionistaId inversionistaId,
                                List<Posicion> posiciones,
                                BigDecimal valorTotal,
                                BigDecimal gananciaNoRealizada) {
}
