package com.g1.ordenes.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransaccionJpaRepository extends JpaRepository<TransaccionEntity, String> {
    List<TransaccionEntity> findByCompradorIdOrVendedorIdOrderByEjecutadaEnDesc(String compradorId, String vendedorId);
}
