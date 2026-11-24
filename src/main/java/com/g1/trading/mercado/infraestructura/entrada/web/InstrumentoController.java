package com.g1.trading.mercado.infraestructura.entrada.web;

import com.g1.trading.mercado.aplicacion.InstrumentoUseCase;
import com.g1.trading.mercado.dominio.HistoricoPrecios;
import com.g1.trading.mercado.dominio.Instrumento;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

/**
 * Adaptador primario. Envuelve HTTP y llama al puerto {@link InstrumentoUseCase},
 * nunca a {@code InstrumentoService} ni al repositorio JPA.
 */
@RestController
@RequestMapping("/instrumentos")
public class InstrumentoController {

    private final InstrumentoUseCase instrumentoUseCase;

    public InstrumentoController(InstrumentoUseCase instrumentoUseCase) {
        this.instrumentoUseCase = instrumentoUseCase;
    }

    @GetMapping
    public List<InstrumentoResponse> listar() {
        return instrumentoUseCase.listarConCotizacionActual().stream()
                .map(InstrumentoMapper::aRespuesta)
                .toList();
    }

    @GetMapping("/{simbolo}/historico")
    public List<CotizacionResponse> historico(@PathVariable String simbolo) {
        HistoricoPrecios historico = instrumentoUseCase.consultarHistorico(simbolo);
        return InstrumentoMapper.aHistorico(historico);
    }

    @PostMapping
    public InstrumentoResponse registrar(@RequestBody RegistrarInstrumentoRequest solicitud) {
        Instrumento instrumento = instrumentoUseCase.registrar(
                solicitud.simbolo(),
                solicitud.nombre(),
                solicitud.tipo(),
                solicitud.precioInicial(),
                Instant.now(),
                solicitud.generadoPor());
        return InstrumentoMapper.aRespuesta(instrumento);
    }

    @PostMapping("/simulacion")
    public List<InstrumentoResponse> simular(@RequestBody SimularPreciosRequest solicitud) {
        instrumentoUseCase.simularPrecios(solicitud.factorVariacion(), Instant.now(), solicitud.generadoPor());
        return listar();
    }
}
