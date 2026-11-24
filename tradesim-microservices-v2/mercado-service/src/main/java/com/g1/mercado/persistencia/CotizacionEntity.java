package com.g1.mercado.persistencia;

import com.g1.mercado.dominio.OrigenCotizacion;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "cotizaciones")
public class CotizacionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instrumento_id", nullable = false)
    private String instrumentoId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal precio;

    @Column(nullable = false)
    private Instant momento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrigenCotizacion origen;

    @Column(name = "generado_por", nullable = false)
    private String generadoPor;

    protected CotizacionEntity() {}

    public CotizacionEntity(String instrumentoId, BigDecimal precio, Instant momento, OrigenCotizacion origen, String generadoPor) {
        this.instrumentoId = instrumentoId;
        this.precio = precio;
        this.momento = momento;
        this.origen = origen;
        this.generadoPor = generadoPor;
    }

    public Long getId() { return id; }
    public String getInstrumentoId() { return instrumentoId; }
    public BigDecimal getPrecio() { return precio; }
    public Instant getMomento() { return momento; }
    public OrigenCotizacion getOrigen() { return origen; }
    public String getGeneradoPor() { return generadoPor; }
}
