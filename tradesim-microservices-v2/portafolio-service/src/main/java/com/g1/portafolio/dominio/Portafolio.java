package com.g1.portafolio.dominio;

import java.math.BigDecimal;
import java.util.*;

public final class Portafolio {
    private final InversionistaId inversionistaId;
    private final Map<InstrumentoId, Posicion> posiciones = new LinkedHashMap<>();

    Portafolio(InversionistaId inversionistaId) {
        if (inversionistaId == null) throw new IllegalArgumentException("El portafolio necesita un inversionista");
        this.inversionistaId = inversionistaId;
    }

    public static Portafolio reconstruir(InversionistaId inversionistaId, List<Posicion> posicionesGuardadas) {
        Portafolio p = new Portafolio(inversionistaId);
        if (posicionesGuardadas != null) {
            for (Posicion pos : posicionesGuardadas) {
                p.posiciones.put(pos.instrumentoId(), new Posicion(pos.instrumentoId(), pos.cantidad(), pos.costoPromedio()));
            }
        }
        return p;
    }

    public void registrarCompra(InstrumentoId instrumentoId, Cantidad cantidad, BigDecimal precio) {
        exigirOperacionValida(instrumentoId, cantidad, precio);
        posiciones.computeIfAbsent(instrumentoId, id -> new Posicion(id, Cantidad.CERO, BigDecimal.ZERO))
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

    public BigDecimal valorTotal(Map<InstrumentoId, BigDecimal> preciosActuales) {
        BigDecimal total = BigDecimal.ZERO;
        for (Posicion p : posiciones.values()) {
            total = total.add(p.valorActual(precioDe(p, preciosActuales)));
        }
        return total;
    }

    public BigDecimal gananciaNoRealizada(Map<InstrumentoId, BigDecimal> preciosActuales) {
        BigDecimal total = BigDecimal.ZERO;
        for (Posicion p : posiciones.values()) {
            total = total.add(p.gananciaNoRealizada(precioDe(p, preciosActuales)));
        }
        return total;
    }

    public List<Posicion> posiciones() { return List.copyOf(new ArrayList<>(posiciones.values())); }
    public InversionistaId inversionistaId() { return inversionistaId; }

    private static BigDecimal precioDe(Posicion posicion, Map<InstrumentoId, BigDecimal> precios) {
        BigDecimal precio = precios.getOrDefault(posicion.instrumentoId(), posicion.costoPromedio());
        if (precio == null || precio.signum() <= 0) {
            throw new IllegalArgumentException("Falta un precio actual válido para " + posicion.instrumentoId().valor());
        }
        return precio;
    }

    private static void exigirOperacionValida(InstrumentoId instrumentoId, Cantidad cantidad, BigDecimal precio) {
        if (instrumentoId == null) throw new IllegalArgumentException("La operación necesita un instrumento");
        if (cantidad == null || cantidad.esCero()) throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        if (precio == null || precio.signum() <= 0) throw new IllegalArgumentException("El precio de la operación debe ser mayor a cero");
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) return true;
        if (!(otro instanceof Portafolio portafolio)) return false;
        return inversionistaId.equals(portafolio.inversionistaId);
    }

    @Override
    public int hashCode() { return Objects.hash(inversionistaId); }
}
