package com.g1.portafolio.dominio;

public final class ValidacionVentaService {

    public boolean puedeVender(Portafolio portafolio, InstrumentoId instrumentoId, Cantidad cantidad) {
        if (portafolio == null || instrumentoId == null || cantidad == null) {
            throw new IllegalArgumentException("Se necesita portafolio, instrumento y cantidad");
        }
        return !cantidad.esCero() && portafolio.poseeCantidad(instrumentoId, cantidad);
    }

    public void validar(Portafolio portafolio, InstrumentoId instrumentoId, Cantidad cantidad) {
        if (!puedeVender(portafolio, instrumentoId, cantidad)) {
            throw new VentaNoPermitidaException("Venta rechazada de " + instrumentoId.valor()
                    + ": el inversionista posee "
                    + portafolio.cantidadDisponible(instrumentoId).valor().toPlainString()
                    + " y solicita " + cantidad.valor().toPlainString());
        }
    }
}
