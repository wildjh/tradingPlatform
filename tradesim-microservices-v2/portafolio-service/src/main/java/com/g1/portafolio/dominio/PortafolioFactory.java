package com.g1.portafolio.dominio;

public final class PortafolioFactory {
    private PortafolioFactory() {}

    public static Portafolio crearVacio(String inversionistaId) {
        if (inversionistaId == null || inversionistaId.isBlank()) {
            throw new IllegalArgumentException("El portafolio necesita un inversionista");
        }
        return new Portafolio(new InversionistaId(inversionistaId.trim()));
    }
}
