package com.g1.trading.mercado.infraestructura.salida.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Puente con Spring Data. Solo lo usa el adaptador.
 * El caso de uso no ve esta interfaz ni los métodos que JpaRepository regala.
 */
public interface InstrumentoJpaRepository extends JpaRepository<InstrumentoJpa, String> {
}
