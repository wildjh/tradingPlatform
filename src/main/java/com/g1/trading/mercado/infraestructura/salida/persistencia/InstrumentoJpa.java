package com.g1.trading.mercado.infraestructura.salida.persistencia;

import com.g1.trading.mercado.dominio.Cotizacion;
import com.g1.trading.mercado.dominio.Instrumento;
import com.g1.trading.mercado.dominio.TipoInstrumento;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Tabla del catálogo. No es el agregado: el adaptador traduce entre esta fila
 * y {@link Instrumento}.
 */
@Entity
@Table(name = "instrumentos")
public class InstrumentoJpa {

    @Id
    private String simbolo;

    private String nombre;

    @Enumerated(EnumType.STRING)
    private TipoInstrumento tipo;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cotizaciones", joinColumns = @JoinColumn(name = "simbolo"))
    @OrderColumn(name = "posicion")
    private List<CotizacionJpa> cotizaciones = new ArrayList<>();

    protected InstrumentoJpa() {
    }

    public String getSimbolo() {
        return simbolo;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoInstrumento getTipo() {
        return tipo;
    }

    public List<CotizacionJpa> getCotizaciones() {
        return cotizaciones;
    }

    public void copiarDesde(Instrumento instrumento) {
        this.simbolo = instrumento.id().valor();
        this.nombre = instrumento.nombre();
        this.tipo = instrumento.tipo();
        this.cotizaciones.clear();
        for (Cotizacion cotizacion : instrumento.consultarHistorico().cotizaciones()) {
            this.cotizaciones.add(new CotizacionJpa(
                    cotizacion.precio().valor(),
                    cotizacion.precio().momento(),
                    cotizacion.origen(),
                    cotizacion.generadoPor()));
        }
    }
}
