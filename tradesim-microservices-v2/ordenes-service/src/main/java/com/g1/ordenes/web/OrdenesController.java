package com.g1.ordenes.web;

import com.g1.ordenes.aplicacion.GestionOrdenesService;
import com.g1.ordenes.dominio.LadoOrden;
import com.g1.ordenes.dominio.TipoOrden;
import com.g1.ordenes.persistencia.OrdenEntity;
import com.g1.ordenes.persistencia.TransaccionEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/ordenes")
public class OrdenesController {

    private final GestionOrdenesService gestionOrdenesService;

    public OrdenesController(GestionOrdenesService gestionOrdenesService) {
        this.gestionOrdenesService = gestionOrdenesService;
    }

    public record CrearOrdenRequest(
            String inversionistaId,
            String instrumentoId,
            LadoOrden lado,
            TipoOrden tipo,
            BigDecimal cantidad,
            BigDecimal precioLimite
    ) {}

    // Coincide exactamente con CrearOrdenResponse en api.ts
    @PostMapping
    public ResponseEntity<GestionOrdenesService.ResultadoCreacionOrden> crearOrden(@RequestBody CrearOrdenRequest req) {
        var resultado = gestionOrdenesService.crearOrden(
                req.inversionistaId(), req.instrumentoId(), req.lado(), req.tipo(), req.cantidad(), req.precioLimite()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    // Coincide exactamente con DELETE /ordenes/{ordenId}?inversionistaId=... en api.ts
    @DeleteMapping("/{ordenId}")
    public ResponseEntity<OrdenEntity> cancelarOrden(@PathVariable String ordenId, @RequestParam String inversionistaId) {
        return ResponseEntity.ok(gestionOrdenesService.cancelarOrden(ordenId, inversionistaId));
    }

    // Coincide exactamente con GET /ordenes?inversionistaId=... en api.ts
    @GetMapping
    public ResponseEntity<List<OrdenEntity>> listarOrdenes(@RequestParam String inversionistaId) {
        return ResponseEntity.ok(gestionOrdenesService.listarOrdenesPorInversionista(inversionistaId));
    }

    // Coincide exactamente con GET /ordenes/libro?instrumentoId=...&lado=... en api.ts
    @GetMapping("/libro")
    public ResponseEntity<List<OrdenEntity>> consultarLibro(
            @RequestParam String instrumentoId,
            @RequestParam LadoOrden lado
    ) {
        return ResponseEntity.ok(gestionOrdenesService.listarLibroPendiente(instrumentoId, lado));
    }

    // Coincide exactamente con GET /ordenes/transacciones?inversionistaId=... en api.ts
    @GetMapping("/transacciones")
    public ResponseEntity<List<TransaccionEntity>> listarTransacciones(@RequestParam String inversionistaId) {
        return ResponseEntity.ok(gestionOrdenesService.listarTransaccionesPorInversionista(inversionistaId));
    }
}
