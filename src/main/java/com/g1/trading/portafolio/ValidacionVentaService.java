package com.g1.trading.portafolio;

import com.g1.trading.mercado.InstrumentoId;

/**
 * Paso 3 · Servicio de dominio: regla "no se puede vender lo que no se posee" (HU-03).
 * Compara lo solicitado (Cantidad) con lo que tiene el Portafolio; no pertenece a ninguno de los dos.
 * Sin Spring. Lo usa Órdenes, vía {@link PortafolioService}, antes de aceptar una orden de venta.
 */
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
