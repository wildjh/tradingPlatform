package com.g1.ordenes.aplicacion;

import com.g1.ordenes.dominio.*;
import com.g1.ordenes.infraestructura.MercadoHttpClient;
import com.g1.ordenes.infraestructura.PortafolioHttpClient;
import com.g1.ordenes.persistencia.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class GestionOrdenesService {

    private final OrdenJpaRepository ordenRepository;
    private final TransaccionJpaRepository transaccionRepository;
    private final EmparejamientoService emparejamientoService;
    private final MercadoHttpClient mercadoClient;
    private final PortafolioHttpClient portafolioClient;

    public GestionOrdenesService(
            OrdenJpaRepository ordenRepository,
            TransaccionJpaRepository transaccionRepository,
            EmparejamientoService emparejamientoService,
            MercadoHttpClient mercadoClient,
            PortafolioHttpClient portafolioClient
    ) {
        this.ordenRepository = ordenRepository;
        this.transaccionRepository = transaccionRepository;
        this.emparejamientoService = emparejamientoService;
        this.mercadoClient = mercadoClient;
        this.portafolioClient = portafolioClient;
    }

    public record ResultadoCreacionOrden(OrdenEntity orden, TransaccionEntity transaccionEjecutada) {}

    @Transactional
    public ResultadoCreacionOrden crearOrden(
            String inversionistaId,
            String instrumentoId,
            LadoOrden lado,
            TipoOrden tipo,
            BigDecimal cantidad,
            BigDecimal precioLimite
    ) {
        if (instrumentoId == null || instrumentoId.isBlank()) {
            throw new IllegalArgumentException("El instrumento es obligatorio");
        }
        String simbolo = instrumentoId.trim().toUpperCase();

        if (lado == LadoOrden.VENTA) {
            portafolioClient.validarVenta(inversionistaId, simbolo, cantidad);
        }

        Orden nuevaOrden = (tipo == TipoOrden.LIMITE)
                ? OrdenFactory.crearLimite(inversionistaId, simbolo, lado, cantidad, precioLimite)
                : OrdenFactory.crearMercado(inversionistaId, simbolo, lado, cantidad);

        OrdenEntity nuevaEntity = OrdenEntity.desdeDominio(nuevaOrden);

        LadoOrden ladoOpuesto = (lado == LadoOrden.COMPRA) ? LadoOrden.VENTA : LadoOrden.COMPRA;
        List<OrdenEntity> candidatas = ordenRepository.findByInstrumentoIdAndLadoAndEstadoOrderByCreadaEnAsc(
                simbolo, ladoOpuesto, EstadoOrden.PENDIENTE
        );

        for (OrdenEntity candidataEntity : candidatas) {
            Orden candidata = candidataEntity.aDominio();
            if (emparejamientoService.sonCompatibles(nuevaOrden, candidata)) {
                BigDecimal precioMercado = mercadoClient.obtenerPrecioActual(simbolo);
                Transaccion tx = emparejamientoService.emparejar(nuevaOrden, candidata, precioMercado);

                portafolioClient.aplicarTransaccion(
                        tx.compradorId(),
                        tx.vendedorId(),
                        tx.instrumentoId(),
                        tx.cantidad(),
                        tx.precioEjecucion()
                );

                nuevaEntity.actualizarEstado(nuevaOrden.estado());
                candidataEntity.actualizarEstado(candidata.estado());
                ordenRepository.save(nuevaEntity);
                ordenRepository.save(candidataEntity);
                TransaccionEntity txEntity = transaccionRepository.save(TransaccionEntity.desdeDominio(tx));

                return new ResultadoCreacionOrden(nuevaEntity, txEntity);
            }
        }

        ordenRepository.save(nuevaEntity);
        return new ResultadoCreacionOrden(nuevaEntity, null);
    }

    @Transactional
    public OrdenEntity cancelarOrden(String ordenId, String inversionistaId) {
        OrdenEntity entity = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada: " + ordenId));

        Orden orden = entity.aDominio();
        if (!orden.inversionistaId().equals(inversionistaId)) {
            throw new IllegalStateException("La orden no pertenece al inversionista indicado");
        }

        orden.cancelar();
        entity.actualizarEstado(orden.estado());
        return ordenRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<OrdenEntity> listarOrdenesPorInversionista(String inversionistaId) {
        return ordenRepository.findByInversionistaIdOrderByCreadaEnDesc(inversionistaId);
    }

    @Transactional(readOnly = true)
    public List<OrdenEntity> listarLibroPendiente(String instrumentoId, LadoOrden lado) {
        String simbolo = instrumentoId.trim().toUpperCase();
        return ordenRepository.findByInstrumentoIdAndLadoAndEstadoOrderByCreadaEnAsc(simbolo, lado, EstadoOrden.PENDIENTE);
    }

    @Transactional(readOnly = true)
    public List<TransaccionEntity> listarTransaccionesPorInversionista(String inversionistaId) {
        return transaccionRepository.findByCompradorIdOrVendedorIdOrderByEjecutadaEnDesc(inversionistaId, inversionistaId);
    }

    @Transactional(readOnly = true)
    public Optional<OrdenEntity> buscarOrdenPorId(String ordenId) {
        return ordenRepository.findById(ordenId);
    }
}
