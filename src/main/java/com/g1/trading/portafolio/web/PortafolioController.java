package com.g1.trading.portafolio.web;

import com.g1.trading.mercado.persistencia.CotizacionEntity;
import com.g1.trading.mercado.persistencia.CotizacionJpaRepository;
import com.g1.trading.mercado.persistencia.InstrumentoJpaRepository;
import com.g1.trading.portafolio.PortafolioService;
import com.g1.trading.portafolio.ResumenPortafolio;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portafolio")
public class PortafolioController {

    private final PortafolioService portafolioService;
    private final InstrumentoJpaRepository instrumentoRepo;
    private final CotizacionJpaRepository cotizacionRepo;

    public PortafolioController(
            PortafolioService portafolioService,
            InstrumentoJpaRepository instrumentoRepo,
            CotizacionJpaRepository cotizacionRepo
    ) {
        this.portafolioService = portafolioService;
        this.instrumentoRepo = instrumentoRepo;
        this.cotizacionRepo = cotizacionRepo;
    }

    public record PosicionResponse(
            String instrumentoId,
            BigDecimal cantidad,
            BigDecimal costoPromedio,
            BigDecimal precioActual,
            BigDecimal valorActual,
            BigDecimal gananciaNoRealizada
    ) {}

    public record PortafolioResponse(
            String inversionistaId,
            List<PosicionResponse> posiciones,
            BigDecimal valorTotal,
            BigDecimal gananciaNoRealizada
    ) {}

    public record CargarPosicionRequest(
            String inversionistaId,
            String instrumentoId,
            BigDecimal cantidad,
            BigDecimal precio
    ) {}

    @GetMapping("/{inversionistaId}")
    public ResponseEntity<PortafolioResponse> consultarPortafolio(@PathVariable String inversionistaId) {
        Map<String, BigDecimal> preciosActuales = obtenerPreciosActuales();
        ResumenPortafolio resumen = portafolioService.consultar(inversionistaId, preciosActuales);

        List<PosicionResponse> posiciones = resumen.posiciones().stream()
                .map(pos -> {
                    String simbolo = pos.instrumentoId().valor();
                    BigDecimal precio = preciosActuales.getOrDefault(simbolo, pos.costoPromedio());
                    return new PosicionResponse(
                            simbolo,
                            pos.cantidad().valor(),
                            pos.costoPromedio(),
                            precio,
                            pos.valorActual(precio),
                            pos.gananciaNoRealizada(precio)
                    );
                })
                .toList();

        return ResponseEntity.ok(new PortafolioResponse(
                resumen.inversionistaId().valor(),
                posiciones,
                resumen.valorTotal(),
                resumen.gananciaNoRealizada()
        ));
    }

    @PostMapping("/cargar")
    public ResponseEntity<PortafolioResponse> cargarPosicionInicial(@RequestBody CargarPosicionRequest req) {
        if (req.instrumentoId() == null || req.instrumentoId().isBlank()) {
            throw new IllegalArgumentException("El instrumento es obligatorio");
        }
        String simbolo = req.instrumentoId().trim().toUpperCase();
        if (!instrumentoRepo.existsById(simbolo)) {
            throw new IllegalArgumentException("Instrumento no registrado en el catálogo: " + simbolo);
        }

        portafolioService.cargarPosicionInicial(
                req.inversionistaId(),
                simbolo,
                req.cantidad(),
                req.precio()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(consultarPortafolio(req.inversionistaId()).getBody());
    }

    private Map<String, BigDecimal> obtenerPreciosActuales() {
        Map<String, BigDecimal> precios = new HashMap<>();
        instrumentoRepo.findAll().forEach(inst ->
                cotizacionRepo.findFirstByInstrumentoIdOrderByMomentoDesc(inst.getId())
                        .map(CotizacionEntity::getPrecio)
                        .ifPresent(precio -> precios.put(inst.getId(), precio))
        );
        return precios;
    }
}