package com.g1.mercado.persistencia;

import com.g1.mercado.dominio.TipoInstrumento;
import jakarta.persistence.*;

@Entity
@Table(name = "instrumentos")
public class InstrumentoEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoInstrumento tipo;

    protected InstrumentoEntity() {}

    public InstrumentoEntity(String id, String nombre, TipoInstrumento tipo) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public TipoInstrumento getTipo() { return tipo; }
}
