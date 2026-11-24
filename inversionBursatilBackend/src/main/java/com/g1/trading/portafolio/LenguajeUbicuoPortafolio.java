package com.g1.trading.portafolio;

import java.util.Map;

/**
 * Lenguaje ubicuo del contexto Portafolio (Paso 1).
 * Cada término significa una sola cosa aquí; no se usan palabras de órdenes ni del catálogo.
 */
public final class LenguajeUbicuoPortafolio {

    private LenguajeUbicuoPortafolio() {
    }

    public static Map<String, String> terminos() {
        return Map.of(
                "Portafolio",
                "Conjunto de posiciones de un inversionista. Un portafolio por inversionista; es la raíz del agregado.",
                "Posicion",
                "Tenencia de un instrumento dentro del portafolio: cantidad y costo promedio.",
                "Cantidad",
                "Unidades de un instrumento. Nunca es negativa.",
                "CostoPromedio",
                "Precio medio pagado por las unidades que se poseen de un instrumento.",
                "ValorTotal",
                "Suma de cantidad por precio actual de todas las posiciones del portafolio.",
                "GananciaNoRealizada",
                "Diferencia entre el valor actual y el costo de las posiciones que aún no se han vendido.",
                "ValidacionVenta",
                "Regla: no se puede vender un instrumento en mayor cantidad de la que se posee.",
                "InstrumentoId",
                "Referencia a un instrumento del catálogo. Se guarda solo el id, nunca el Instrumento."
        );
    }
}
