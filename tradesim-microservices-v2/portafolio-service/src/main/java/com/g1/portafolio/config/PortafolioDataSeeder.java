package com.g1.portafolio.config;

import com.g1.portafolio.aplicacion.PortafolioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PortafolioDataSeeder implements CommandLineRunner {

    private final PortafolioService portafolioService;

    public PortafolioDataSeeder(PortafolioService portafolioService) {
        this.portafolioService = portafolioService;
    }

    @Override
    public void run(String... args) {
        if (portafolioService.consultar("INV-01", java.util.Map.of()).posiciones().isEmpty()) {
            portafolioService.cargarPosicionInicial("INV-01", "AAPL", new BigDecimal("50"), new BigDecimal("180.00"));
            portafolioService.cargarPosicionInicial("INV-01", "MSFT", new BigDecimal("20"), new BigDecimal("410.00"));
            portafolioService.cargarPosicionInicial("INV-02", "SPY", new BigDecimal("15"), new BigDecimal("505.00"));
        }
    }
}
