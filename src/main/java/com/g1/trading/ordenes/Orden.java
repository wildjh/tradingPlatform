package com.g1.trading.ordenes;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Raíz del agregado de Órdenes.
 *
 * Controla el ciclo de vida: PENDIENTE → EJECUTADA o CANCELADA.
 * Las referencias a otros contextos se guardan por identificador.
 *
 * El constructor tiene acceso de paquete para utilizarlo desde
 * la Factory que crearemos después.
 */
public final class Orden {

    private final String id;
    private final String inversionistaId;
    private final String instrumentoId;
    private final LadoOrden lado;
    private final TipoOrden tipo;
    private final BigDecimal cantidad;
    private final PrecioLimite precioLimite;

    private EstadoOrden estado;

    Orden(
            String id,
            String inversionistaId,
            String instrumentoId,
            LadoOrden lado,
            TipoOrden tipo,
            BigDecimal cantidad,
            PrecioLimite precioLimite
    ) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "La orden necesita un id"
            );
        }
        if (inversionistaId == null || inversionistaId.isBlank()) {
            throw new IllegalArgumentException(
                    "La orden necesita un inversionista"
            );
        }
        if (instrumentoId == null || instrumentoId.isBlank()) {
            throw new IllegalArgumentException(
                    "La orden necesita un instrumento"
            );
        }
        if (lado == null) {
            throw new IllegalArgumentException(
                    "La orden necesita un lado: COMPRA o VENTA"
            );
        }
        if (tipo == null) {
            throw new IllegalArgumentException(
                    "La orden necesita un tipo: MERCADO o LIMITE"
            );
        }
        if (cantidad == null
                || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor a cero"
            );
        }
        if (tipo == TipoOrden.LIMITE && precioLimite == null) {
            throw new IllegalArgumentException(
                    "Una orden límite necesita un precio límite"
            );
        }
        if (tipo == TipoOrden.MERCADO && precioLimite != null) {
            throw new IllegalArgumentException(
                    "Una orden a mercado no lleva precio límite"
            );
        }

        this.id = id.trim();
        this.inversionistaId = inversionistaId.trim();
        this.instrumentoId = instrumentoId.trim();
        this.lado = lado;
        this.tipo = tipo;
        this.cantidad = cantidad.stripTrailingZeros();
        this.precioLimite = precioLimite;
        this.estado = EstadoOrden.PENDIENTE;
    }

    /**
     * Cancela únicamente una orden pendiente.
     */
    public void cancelar() {
        verificarPendiente();
        estado = EstadoOrden.CANCELADA;
    }

    /**
     * Marca la ejecución completa.
     * Acceso de paquete para utilizarlo desde el motor de emparejamiento.
     */
    void ejecutar() {
        verificarPendiente();
        estado = EstadoOrden.EJECUTADA;
    }

    private void verificarPendiente() {
        if (!estaPendiente()) {
            throw new IllegalStateException(
                    "La orden ya está " + estado
                            + " y no puede cambiar de estado"
            );
        }
    }

    public boolean estaPendiente() {
        return estado == EstadoOrden.PENDIENTE;
    }

    public String id() {
        return id;
    }

    public String inversionistaId() {
        return inversionistaId;
    }

    public String instrumentoId() {
        return instrumentoId;
    }

    public LadoOrden lado() {
        return lado;
    }

    public TipoOrden tipo() {
        return tipo;
    }

    public BigDecimal cantidad() {
        return cantidad;
    }

    /**
     * Devuelve null para órdenes a mercado.
     */
    public PrecioLimite precioLimite() {
        return precioLimite;
    }

    public EstadoOrden estado() {
        return estado;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof Orden orden)) {
            return false;
        }
        return id.equals(orden.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}