package com.g1.trading.ordenes;

import java.util.Map;

/**
 * Lenguaje ubicuo del contexto Órdenes y emparejamiento.
 * Cada término tiene un significado concreto dentro de este contexto.
 */
public final class LenguajeUbicuoOrdenes {

    private LenguajeUbicuoOrdenes() {
    }

    public static Map<String, String> terminos() {
        return Map.of(
                "Orden",
                "Solicitud de un inversionista para comprar o vender una cantidad de un instrumento identificado por su id.",
                "Compra",
                "Lado de una orden que busca adquirir un instrumento.",
                "Venta",
                "Lado de una orden que busca vender un instrumento.",
                "PrecioLimite",
                "Precio mayor que cero: máximo que se acepta pagar en una compra o mínimo que se acepta recibir en una venta.",
                "EstadoOrden",
                "Situación de una orden: PENDIENTE, EJECUTADA o CANCELADA.",
                "Emparejamiento",
                "Cruce de órdenes pendientes del mismo instrumento y de lados opuestos, donde el precio de compra es mayor o igual al precio de venta.",
                "Transaccion",
                "Registro de la operación realizada al ejecutar un cruce entre una orden de compra y una de venta."
        );
    }
}