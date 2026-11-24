package com.g1.trading.ordenes;

import java.util.List;

/** Puerto de salida para registrar y auditar transacciones ejecutadas. */
public interface TransaccionRepository {

    void guardar(Transaccion transaccion);

    List<Transaccion> buscarPorInversionista(String inversionistaId);

    List<Transaccion> listarTodas();
}
