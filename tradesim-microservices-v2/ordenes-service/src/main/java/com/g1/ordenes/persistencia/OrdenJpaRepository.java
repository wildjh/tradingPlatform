package com.g1.ordenes.persistencia;

import com.g1.ordenes.dominio.EstadoOrden;
import com.g1.ordenes.dominio.LadoOrden;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrdenJpaRepository extends JpaRepository<OrdenEntity, String> {
    List<OrdenEntity> findByInversionistaIdOrderByCreadaEnDesc(String inversionistaId);
    List<OrdenEntity> findByInstrumentoIdAndLadoAndEstadoOrderByCreadaEnAsc(String instrumentoId, LadoOrden lado, EstadoOrden estado);
}
