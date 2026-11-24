package com.g1.ordenes.persistencia;

import com.g1.ordenes.dominio.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "ordenes")
public class OrdenEntity {
    @Id
    private String id;

    @Column(name = "inversionista_id", nullable = false)
    private String inversionistaId;

    @Column(name = "instrumento_id", nullable = false)
    private String instrumentoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LadoOrden lado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoOrden tipo;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal cantidad;

    @Column(name = "precio_limite", precision = 19, scale = 4)
    private BigDecimal precioLimite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoOrden estado;

    @Column(name = "creada_en", nullable = false, updatable = false)
    private Instant creadaEn;

    protected OrdenEntity() {}

    public static OrdenEntity desdeDominio(Orden orden) {
        OrdenEntity entity = new OrdenEntity();
        entity.id = orden.id();
        entity.inversionistaId = orden.inversionistaId();
        entity.instrumentoId = orden.instrumentoId();
        entity.lado = orden.lado();
        entity.tipo = orden.tipo();
        entity.cantidad = orden.cantidad();
        entity.precioLimite = orden.precioLimite() != null ? orden.precioLimite().valor() : null;
        entity.estado = orden.estado();
        entity.creadaEn = Instant.now();
        return entity;
    }

    public Orden aDominio() {
        PrecioLimite limite = this.precioLimite != null ? new PrecioLimite(this.precioLimite) : null;
        return Orden.reconstruir(id, inversionistaId, instrumentoId, lado, tipo, cantidad, limite, estado);
    }

    public void actualizarEstado(EstadoOrden nuevoEstado) { this.estado = nuevoEstado; }
    public String getId() { return id; }
    public String getInversionistaId() { return inversionistaId; }
    public String getInstrumentoId() { return instrumentoId; }
    public LadoOrden getLado() { return lado; }
    public TipoOrden getTipo() { return tipo; }
    public BigDecimal getCantidad() { return cantidad; }
    public BigDecimal getPrecioLimite() { return precioLimite; }
    public EstadoOrden getEstado() { return estado; }
    public Instant getCreadaEn() { return creadaEn; }
}
