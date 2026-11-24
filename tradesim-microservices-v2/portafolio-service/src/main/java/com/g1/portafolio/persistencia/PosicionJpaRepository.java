package com.g1.portafolio.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PosicionJpaRepository extends JpaRepository<PosicionEntity, Long> {
    List<PosicionEntity> findByInversionistaId(String inversionistaId);
    void deleteByInversionistaId(String inversionistaId);
}
