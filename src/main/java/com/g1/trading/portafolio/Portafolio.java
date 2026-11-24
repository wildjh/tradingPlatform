package com.g1.trading.portafolio;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.g1.trading.compartido.InversionistaId;
import com.g1.trading.mercado.InstrumentoId;

/**
 * Paso 4 · Raíz del agregado Portafolio (un portafolio por inversionista).
 * <p>
 * Referencia a otros contextos solo por id (InversionistaId, InstrumentoId de compartido).
 * Las posiciones se modifican únicamente desde aquí. La única forma de obtener una instancia
 * es {@link PortafolioFactory}: el constructor no es público.
 */
public final class Portafolio {

    private final InversionistaId inversionistaId;
    private final Map<InstrumentoId, Posicion> posiciones = new LinkedHashMap<>();

    Portafolio(InversionistaId inversionistaId) {
        if (inversionistaId == null) {
            throw new IllegalArgumentException("El portafolio necesita un inversionista");
        }
        this.inversionistaId = inversionistaId;
    }

    public void registrarCompra(InstrumentoId instrumentoId, Cantidad cantidad, BigDecimal precio) {
        exigirOperacionValida(instrumentoId, cantidad, precio);
        posiciones.computeIfAbsent(instrumentoId,
                id -> new Posicion(id, Cantidad.CERO, BigDecimal.ZERO))
                .agregarCompra(cantidad, precio);
    }

    public void registrarVenta(InstrumentoId instrumentoId, Cantidad cantidad, BigDecimal precio) {
        exigirOperacionValida(instrumentoId, cantidad, precio);
        if (!poseeCantidad(instrumentoId, cantidad)) {
            throw new VentaNoPermitidaException("Cantidad insuficiente de " + instrumentoId.valor()
                    + ": disponible " + cantidadDisponible(instrumentoId).valor().toPlainString()
                    + ", solicitada " + cantidad.valor().toPlainString());
        }
        Posicion posicion = posiciones.get(instrumentoId);
        posicion.reducir(cantidad);
        if (posicion.cantidad().esCero()) {
            posiciones.remove(instrumentoId);
        }
    }

    public Cantidad cantidadDisponible(InstrumentoId instrumentoId) {
        Posicion posicion = posiciones.get(instrumentoId);
        return posicion == null ? Cantidad.CERO : posicion.cantidad();
    }

    public boolean poseeCantidad(InstrumentoId instrumentoId, Cantidad requerida) {
        return cantidadDisponible(instrumentoId).esMayorOIgualQue(requerida);
    }

    /** HU-06: valor total con los precios actuales recibidos por id de instrumento. */
    public BigDecimal valorTotal(Map<InstrumentoId, BigDecimal> preciosActuales) {
        BigDecimal total = BigDecimal.ZERO;
        for (Posicion p : posiciones.values()) {
            total = total.add(p.valorActual(precioDe(p, preciosActuales)));
        }
        return total;
    }

    /** HU-06: ganancia o pérdida no realizada total. */
    public BigDecimal gananciaNoRealizada(Map<InstrumentoId, BigDecimal> preciosActuales) {
        BigDecimal total = BigDecimal.ZERO;
        for (Posicion p : posiciones.values()) {
            total = total.add(p.gananciaNoRealizada(precioDe(p, preciosActuales)));
        }
        return total;
    }

    /** Copia: consultar no permite alterar el agregado. */
    public List<Posicion> posiciones() {
        return List.copyOf(new ArrayList<>(posiciones.values()));
    }

    public InversionistaId inversionistaId() {
        return inversionistaId;
    }

    private static BigDecimal precioDe(Posicion posicion, Map<InstrumentoId, BigDecimal> precios) {
        BigDecimal precio = precios.get(posicion.instrumentoId());
        if (precio == null || precio.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Falta un precio actual válido para " + posicion.instrumentoId().valor());
        }
        return precio;
    }

    private static void exigirOperacionValida(InstrumentoId instrumentoId, Cantidad cantidad, BigDecimal precio) {
        if (instrumentoId == null) {
            throw new IllegalArgumentException("La operación necesita un instrumento");
        }
        if (cantidad == null || cantidad.esCero()) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        if (precio == null || precio.signum() <= 0) {
            throw new IllegalArgumentException("El precio de la operación debe ser mayor a cero");
        }
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof Portafolio portafolio)) {
            return false;
        }
        return inversionistaId.equals(portafolio.inversionistaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inversionistaId);
    }
}
