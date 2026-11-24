package com.g1.mercado.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InstrumentoJpaRepository extends JpaRepository<InstrumentoEntity, String> {
}
