package com.g1.portafolio.dominio;

public class VentaNoPermitidaException extends RuntimeException {
    public VentaNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}
