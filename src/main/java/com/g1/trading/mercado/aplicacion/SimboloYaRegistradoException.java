package com.g1.trading.mercado.aplicacion;

/** El catálogo ya tiene un instrumento con ese símbolo. */
public class SimboloYaRegistradoException extends RuntimeException {

    public SimboloYaRegistradoException(String simbolo) {
        super("Ya existe un instrumento con símbolo " + simbolo);
    }
}
