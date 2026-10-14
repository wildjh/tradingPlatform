package com.g1.trading.portafolio;

import com.g1.trading.compartido.InversionistaId;

import java.math.BigDecimal;
import java.util.List;

/** HU-06: estado del portafolio para consulta (posiciones, valor total, ganancia/pérdida no realizada). */
public record ResumenPortafolio(InversionistaId inversionistaId,
                                List<Posicion> posiciones,
                                BigDecimal valorTotal,
                                BigDecimal gananciaNoRealizada) {
}
