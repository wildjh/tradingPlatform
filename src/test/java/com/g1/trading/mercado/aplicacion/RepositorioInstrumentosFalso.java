package com.g1.trading.mercado.aplicacion;

import com.g1.trading.mercado.dominio.Instrumento;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Doble de prueba en memoria. Cumple el puerto secundario sin Spring ni base de datos. */
class RepositorioInstrumentosFalso implements RepositorioInstrumentos {

    private final Map<String, Instrumento> almacen = new LinkedHashMap<>();

    @Override
    public List<Instrumento> listarTodos() {
        return new ArrayList<>(almacen.values());
    }

    @Override
    public Optional<Instrumento> buscarPorSimbolo(String simbolo) {
        return Optional.ofNullable(almacen.get(simbolo));
    }

    @Override
    public boolean existeSimbolo(String simbolo) {
        return almacen.containsKey(simbolo);
    }

    @Override
    public Instrumento guardar(Instrumento instrumento) {
        almacen.put(instrumento.id().valor(), instrumento);
        return instrumento;
    }
}
