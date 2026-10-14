/**
 * Bounded Context Portafolio (Dev 3 · David Cruz).
 * Posiciones del inversionista, valor total y ganancia/pérdida no realizada.
 * <p>
 * Límite del agregado: la raíz es {@link com.g1.trading.portafolio.Portafolio}; Posicion solo se
 * modifica a través de ella. Las referencias a otros contextos son únicamente por id
 * ({@code InversionistaId}, {@code InstrumentoId} de {@code compartido}); no se importa ninguna
 * clase de {@code mercado} ni de {@code ordenes}. Los precios actuales llegan como datos
 * (símbolo -> precio), nunca como objetos de Mercado.
 */
package com.g1.trading.portafolio;
