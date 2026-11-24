package com.g1.trading.config;

import com.g1.trading.mercado.aplicacion.InstrumentoUseCase;
import com.g1.trading.mercado.dominio.TipoInstrumento;
import com.g1.trading.portafolio.PortafolioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Carga el catálogo inicial de instrumentos y tenencias base al iniciar la aplicación
 * cuando el catálogo aún está vacío.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final InstrumentoUseCase instrumentoUseCase;
    private final PortafolioService portafolioService;

    public DataSeeder(
            InstrumentoUseCase instrumentoUseCase,
            PortafolioService portafolioService
    ) {
        this.instrumentoUseCase = instrumentoUseCase;
        this.portafolioService = portafolioService;
    }

    @Override
    public void run(String... args) {
        if (instrumentoUseCase.listarConCotizacionActual().isEmpty()) {
            Instant ahora = Instant.now();
            instrumentoUseCase.registrar("AAPL", "Apple Inc.", TipoInstrumento.ACCION, new BigDecimal("185.50"), ahora, "SEEDER");
            instrumentoUseCase.registrar("MSFT", "Microsoft Corp.", TipoInstrumento.ACCION, new BigDecimal("420.00"), ahora, "SEEDER");
            instrumentoUseCase.registrar("SPY", "SPDR S&P 500 ETF", TipoInstrumento.ETF, new BigDecimal("510.25"), ahora, "SEEDER");

            portafolioService.cargarPosicionInicial("INV-01", "AAPL", new BigDecimal("50"), new BigDecimal("180.00"));
            portafolioService.cargarPosicionInicial("INV-01", "MSFT", new BigDecimal("20"), new BigDecimal("410.00"));
            portafolioService.cargarPosicionInicial("INV-02", "SPY", new BigDecimal("15"), new BigDecimal("505.00"));
        }
    }
}
