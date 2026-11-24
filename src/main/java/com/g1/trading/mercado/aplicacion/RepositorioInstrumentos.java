package com.g1.trading.mercado.aplicacion;

import com.g1.trading.mercado.dominio.Instrumento;

import java.util.List;
import java.util.Optional;

/**
 * Puerto secundario mínimo.
 * Solo los métodos que el caso de uso llama de verdad. No extiende JpaRepository:
 * el núcleo no conoce Spring Data.
 * El símbolo que recibe ya está normalizado (por ejemplo AAPL).
 */
public interface RepositorioInstrumentos {

    List<Instrumento> listarTodos();

    Optional<Instrumento> buscarPorSimbolo(String simbolo);

    boolean existeSimbolo(String simbolo);

    Instrumento guardar(Instrumento instrumento);
}
