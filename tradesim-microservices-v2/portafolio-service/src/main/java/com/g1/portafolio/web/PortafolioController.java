package com.g1.portafolio.web;

import com.g1.portafolio.aplicacion.PortafolioService;
import com.g1.portafolio.dominio.ResumenPortafolio;
import com.g1.portafolio.infraestructura.MercadoHttpClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portafolio")
public class PortafolioController {

    private final PortafolioService portafolioService;
    private final MercadoHttpClient mercadoClient;

    public PortafolioController(PortafolioService portafolioService, MercadoHttpClient mercadoClient) {
        this.portafolioService = portafolioService;
        this.mercadoClient = mercadoClient;
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

    public record ValidarVentaRequest(
            String inversionistaId,
            String instrumentoId,
            BigDecimal cantidad
    ) {}

    public record AplicarTransaccionRequest(
            String compradorId,
            String vendedorId,
            String instrumentoId,
            BigDecimal cantidad,
            BigDecimal precio
    ) {}

    @GetMapping("/{inversionistaId}")
    public ResponseEntity<PortafolioResponse> consultarPortafolio(@PathVariable String inversionistaId) {
        Map<String, BigDecimal> preciosActuales = mercadoClient.obtenerPreciosActuales();
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
        if (!mercadoClient.existeInstrumento(simbolo)) {
            throw new IllegalArgumentException("Instrumento no registrado en el catálogo: " + simbolo);
        }

        portafolioService.cargarPosicionInicial(req.inversionistaId(), simbolo, req.cantidad(), req.precio());
        return ResponseEntity.status(HttpStatus.CREATED).body(consultarPortafolio(req.inversionistaId()).getBody());
    }

    @PostMapping("/validar-venta")
    public ResponseEntity<Void> validarVenta(@RequestBody ValidarVentaRequest req) {
        portafolioService.validarVenta(req.inversionistaId(), req.instrumentoId(), req.cantidad());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/transacciones")
    public ResponseEntity<Void> aplicarTransaccion(@RequestBody AplicarTransaccionRequest req) {
        portafolioService.aplicarTransaccion(
                req.compradorId(),
                req.vendedorId(),
                req.instrumentoId(),
                req.cantidad(),
                req.precio()
        );
        return ResponseEntity.ok().build();
    }
}
