package com.g1.trading.portafolio;

import com.g1.trading.compartido.InversionistaId;

/**
 * Paso 5 · Factory: única puerta para crear un Portafolio.
 * Recibe el id como texto (igual que OrdenFactory e InstrumentoFactory), lo valida y entrega
 * una raíz que ya cumple sus invariantes.
 */
public final class PortafolioFactory {

    private PortafolioFactory() {
    }

    public static Portafolio crearVacio(String inversionistaId) {
        if (inversionistaId == null || inversionistaId.isBlank()) {
            throw new IllegalArgumentException("El portafolio necesita un inversionista");
        }
        return new Portafolio(new InversionistaId(inversionistaId.trim()));
    }
}
