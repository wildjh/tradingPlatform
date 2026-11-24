package com.g1.portafolio.persistencia;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "posiciones")
public class PosicionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inversionista_id", nullable = false)
    private String inversionistaId;

    @Column(name = "instrumento_id", nullable = false)
    private String instrumentoId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal cantidad;

    @Column(name = "costo_promedio", nullable = false, precision = 19, scale = 4)
    private BigDecimal costoPromedio;

    protected PosicionEntity() {}

    public PosicionEntity(String inversionistaId, String instrumentoId, BigDecimal cantidad, BigDecimal costoPromedio) {
        this.inversionistaId = inversionistaId;
        this.instrumentoId = instrumentoId;
        this.cantidad = cantidad;
        this.costoPromedio = costoPromedio;
    }

    public Long getId() { return id; }
    public String getInversionistaId() { return inversionistaId; }
    public String getInstrumentoId() { return instrumentoId; }
    public BigDecimal getCantidad() { return cantidad; }
    public BigDecimal getCostoPromedio() { return costoPromedio; }
}
