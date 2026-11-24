package com.g1.ordenes.persistencia;

import com.g1.ordenes.dominio.Transaccion;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transacciones")
public class TransaccionEntity {
    @Id
    private String id;

    @Column(name = "orden_compra_id", nullable = false)
    private String ordenCompraId;

    @Column(name = "orden_venta_id", nullable = false)
    private String ordenVentaId;

    @Column(name = "comprador_id", nullable = false)
    private String compradorId;

    @Column(name = "vendedor_id", nullable = false)
    private String vendedorId;

    @Column(name = "instrumento_id", nullable = false)
    private String instrumentoId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal cantidad;

    @Column(name = "precio_ejecucion", nullable = false, precision = 19, scale = 4)
    private BigDecimal precioEjecucion;

    @Column(name = "ejecutada_en", nullable = false)
    private Instant ejecutadaEn;

    protected TransaccionEntity() {}

    public static TransaccionEntity desdeDominio(Transaccion tx) {
        TransaccionEntity entity = new TransaccionEntity();
        entity.id = tx.id();
        entity.ordenCompraId = tx.ordenCompraId();
        entity.ordenVentaId = tx.ordenVentaId();
        entity.compradorId = tx.compradorId();
        entity.vendedorId = tx.vendedorId();
        entity.instrumentoId = tx.instrumentoId();
        entity.cantidad = tx.cantidad();
        entity.precioEjecucion = tx.precioEjecucion();
        entity.ejecutadaEn = tx.ejecutadaEn();
        return entity;
    }

    public String getId() { return id; }
    public String getOrdenCompraId() { return ordenCompraId; }
    public String getOrdenVentaId() { return ordenVentaId; }
    public String getCompradorId() { return compradorId; }
    public String getVendedorId() { return vendedorId; }
    public String getInstrumentoId() { return instrumentoId; }
    public BigDecimal getCantidad() { return cantidad; }
    public BigDecimal getPrecioEjecucion() { return precioEjecucion; }
    public Instant getEjecutadaEn() { return ejecutadaEn; }
}
