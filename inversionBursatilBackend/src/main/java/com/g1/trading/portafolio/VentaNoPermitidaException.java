package com.g1.trading.portafolio;

/** Se lanza cuando se intenta vender más de lo que el inversionista posee. */
public class VentaNoPermitidaException extends RuntimeException {

    public VentaNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}
