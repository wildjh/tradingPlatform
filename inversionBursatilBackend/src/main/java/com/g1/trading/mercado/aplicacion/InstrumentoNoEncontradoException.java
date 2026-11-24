package com.g1.trading.mercado.aplicacion;

/** No hay un instrumento con ese símbolo en el catálogo. */
public class InstrumentoNoEncontradoException extends RuntimeException {

    public InstrumentoNoEncontradoException(String simbolo) {
        super("No existe el instrumento " + simbolo);
    }
}
