package com.g1.trading.ordenes;

import java.util.List;
import java.util.Optional;

/** Puerto de salida para persistir y consultar órdenes en el libro. */
public interface OrdenRepository {

    void guardar(Orden orden);

    Optional<Orden> buscarPorId(String ordenId);

    List<Orden> buscarPendientesPorInstrumentoYLado(String instrumentoId, LadoOrden lado);

    List<Orden> buscarPorInversionista(String inversionistaId);
}
