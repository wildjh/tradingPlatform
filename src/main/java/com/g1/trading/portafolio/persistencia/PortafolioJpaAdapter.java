package com.g1.trading.portafolio.persistencia;

import com.g1.trading.compartido.InversionistaId;
import com.g1.trading.mercado.InstrumentoId;
import com.g1.trading.portafolio.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class PortafolioJpaAdapter implements PortafolioRepository {

    private final PosicionJpaRepository jpaRepository;

    public PortafolioJpaAdapter(PosicionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Portafolio> buscarPorInversionista(InversionistaId inversionistaId) {
        List<PosicionEntity> filas = jpaRepository.findByInversionistaId(inversionistaId.valor());
        if (filas.isEmpty()) {
            return Optional.empty();
        }
        List<Posicion> posiciones = filas.stream()
                .map(f -> Posicion.reconstruir(
                        new InstrumentoId(f.getInstrumentoId()),
                        f.getCantidad(),
                        f.getCostoPromedio()))
                .toList();
        return Optional.of(Portafolio.reconstruir(inversionistaId, posiciones));
    }

    @Override
    @Transactional
    public void guardar(Portafolio portafolio) {
        String invId = portafolio.inversionistaId().valor();
        jpaRepository.deleteByInversionistaId(invId);
        jpaRepository.flush();

        List<PosicionEntity> nuevas = portafolio.posiciones().stream()
                .map(p -> new PosicionEntity(
                        invId,
                        p.instrumentoId().valor(),
                        p.cantidad().valor(),
                        p.costoPromedio()))
                .toList();
        jpaRepository.saveAll(nuevas);
    }
}