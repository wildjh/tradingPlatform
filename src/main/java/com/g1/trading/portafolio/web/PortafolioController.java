package com.g1.trading.portafolio.web;

import com.g1.trading.mercado.aplicacion.InstrumentoUseCase;
import com.g1.trading.mercado.dominio.Instrumento;
import com.g1.trading.portafolio.PortafolioService;
import com.g1.trading.portafolio.ResumenPortafolio;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adaptador HTTP de Portafolio.
 * Para precios y existencia de instrumentos usa el puerto primario de Mercado
 * ({@link InstrumentoUseCase}), nunca las clases JPA de Mercado.
 */
@RestController
@RequestMapping("/api/portafolio")
public class PortafolioController {

    private final PortafolioService portafolioService;
    private final InstrumentoUseCase instrumentoUseCase;

    public PortafolioController(PortafolioService portafolioService, InstrumentoUseCase instrumentoUseCase) {
        this.portafolioService = portafolioService;
        this.instrumentoUseCase = instrumentoUseCase;
    }

    public record PosicionResponse(
            String instrumentoId,
            BigDecimal cantidad,
            BigDecimal costoPromedio,
            BigDecimal precioActual,
            BigDecimal valorActual,
            BigDecimal gananciaNoRealizada
    ) {
    }

    public record PortafolioResponse(
            String inversionistaId,
            List<PosicionResponse> posiciones,
            BigDecimal valorTotal,
            BigDecimal gananciaNoRealizada
    ) {
    }

    public record CargarPosicionRequest(
            String inversionistaId,
            String instrumentoId,
            BigDecimal cantidad,
            BigDecimal precio
    ) {
    }

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
        if (!existeEnCatalogo(simbolo)) {
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

    /** HU-01 vía puerto de Mercado: mapa símbolo → precio vigente. */
    private Map<String, BigDecimal> obtenerPreciosActuales() {
        Map<String, BigDecimal> precios = new HashMap<>();
        for (Instrumento instrumento : instrumentoUseCase.listarConCotizacionActual()) {
            precios.put(
                    instrumento.id().valor(),
                    instrumento.obtenerCotizacionActual().precio().valor()
            );
        }
        return precios;
    }

    private boolean existeEnCatalogo(String simbolo) {
        return instrumentoUseCase.listarConCotizacionActual().stream()
                .anyMatch(instrumento -> instrumento.id().valor().equals(simbolo));
    }
}
