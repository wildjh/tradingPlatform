package com.g1.mercado.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CotizacionJpaRepository extends JpaRepository<CotizacionEntity, Long> {
    Optional<CotizacionEntity> findFirstByInstrumentoIdOrderByMomentoDesc(String instrumentoId);
    List<CotizacionEntity> findByInstrumentoIdOrderByMomentoAsc(String instrumentoId);
}
