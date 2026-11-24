package com.g1.trading.mercado.infraestructura.salida.persistencia;

import com.g1.trading.mercado.aplicacion.RepositorioInstrumentos;
import com.g1.trading.mercado.dominio.Cotizacion;
import com.g1.trading.mercado.dominio.Instrumento;
import com.g1.trading.mercado.dominio.InstrumentoId;
import com.g1.trading.mercado.dominio.PrecioCotizado;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador secundario. Implementa el puerto del núcleo usando JPA,
 * y traduce la fila {@link InstrumentoJpa} al agregado {@link Instrumento}.
 */
@Component
public class InstrumentoRepositorioJpaAdapter implements RepositorioInstrumentos {

    private final InstrumentoJpaRepository jpaRepository;

    public InstrumentoRepositorioJpaAdapter(InstrumentoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Instrumento> listarTodos() {
        return jpaRepository.findAll().stream().map(this::aDominio).toList();
    }

    @Override
    public Optional<Instrumento> buscarPorSimbolo(String simbolo) {
        return jpaRepository.findById(simbolo).map(this::aDominio);
    }

    @Override
    public boolean existeSimbolo(String simbolo) {
        return jpaRepository.existsById(simbolo);
    }

    @Override
    public Instrumento guardar(Instrumento instrumento) {
        InstrumentoJpa fila = jpaRepository.findById(instrumento.id().valor()).orElseGet(InstrumentoJpa::new);
        fila.copiarDesde(instrumento);
        return aDominio(jpaRepository.save(fila));
    }

    private Instrumento aDominio(InstrumentoJpa fila) {
        List<Cotizacion> cotizaciones = fila.getCotizaciones().stream()
                .map(filaCotizacion -> new Cotizacion(
                        new PrecioCotizado(filaCotizacion.getValor(), filaCotizacion.getMomento()),
                        filaCotizacion.getOrigen(),
                        filaCotizacion.getGeneradoPor()))
                .toList();
        return Instrumento.reconstituir(
                new InstrumentoId(fila.getSimbolo()),
                fila.getNombre(),
                fila.getTipo(),
                cotizaciones);
    }
}
