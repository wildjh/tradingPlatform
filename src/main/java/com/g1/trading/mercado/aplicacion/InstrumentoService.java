package com.g1.trading.mercado.aplicacion;

import com.g1.trading.mercado.dominio.HistoricoPrecios;
import com.g1.trading.mercado.dominio.Instrumento;
import com.g1.trading.mercado.dominio.InstrumentoId;
import com.g1.trading.mercado.dominio.SimulacionPreciosService;
import com.g1.trading.mercado.dominio.TipoInstrumento;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Caso de uso de Mercado. Implementa el puerto primario y habla con el
 * catálogo solo a través de {@link RepositorioInstrumentos}.
 */
@Service
public class InstrumentoService implements InstrumentoUseCase {

    private final RepositorioInstrumentos repositorio;
    private final InstrumentoFactory factory;
    private final SimulacionPreciosService simulacion;

    public InstrumentoService(RepositorioInstrumentos repositorio, InstrumentoFactory factory,
                              SimulacionPreciosService simulacion) {
        this.repositorio = repositorio;
        this.factory = factory;
        this.simulacion = simulacion;
    }

    @Override
    public List<Instrumento> listarConCotizacionActual() {
        return repositorio.listarTodos();
    }

    @Override
    public HistoricoPrecios consultarHistorico(String simbolo) {
        return buscar(simbolo).consultarHistorico();
    }

    @Override
    public Instrumento registrar(String simbolo, String nombre, TipoInstrumento tipo,
                                 BigDecimal precioInicial, Instant momento, String generadoPor) {
        String simboloNormalizado = new InstrumentoId(simbolo).valor();
        if (repositorio.existeSimbolo(simboloNormalizado)) {
            throw new SimboloYaRegistradoException(simboloNormalizado);
        }
        Instrumento instrumento = factory.registrar(simbolo, nombre, tipo, precioInicial, momento, generadoPor);
        return repositorio.guardar(instrumento);
    }

    @Override
    public void simularPrecios(BigDecimal factorVariacion, Instant momento, String generadoPor) {
        List<Instrumento> catalogo = repositorio.listarTodos();
        simulacion.simular(catalogo, factorVariacion, momento, generadoPor);
        for (Instrumento instrumento : catalogo) {
            repositorio.guardar(instrumento);
        }
    }

    private Instrumento buscar(String simbolo) {
        String simboloNormalizado = new InstrumentoId(simbolo).valor();
        return repositorio.buscarPorSimbolo(simboloNormalizado)
                .orElseThrow(() -> new InstrumentoNoEncontradoException(simboloNormalizado));
    }
}
