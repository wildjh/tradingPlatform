package com.g1.trading.config;

import com.g1.trading.ordenes.EmparejamientoService;
import com.g1.trading.ordenes.OrdenRepository;
import com.g1.trading.ordenes.OrdenRepositoryEnMemoria;
import com.g1.trading.ordenes.OrdenService;
import com.g1.trading.ordenes.TransaccionRepository;
import com.g1.trading.ordenes.TransaccionRepositoryEnMemoria;
import com.g1.trading.portafolio.PortafolioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TradingConfig {

    @Bean
    public EmparejamientoService emparejamientoService() {
        return new EmparejamientoService();
    }

    @Bean
    public OrdenRepository ordenRepository() {
        return new OrdenRepositoryEnMemoria();
    }

    @Bean
    public TransaccionRepository transaccionRepository() {
        return new TransaccionRepositoryEnMemoria();
    }

    @Bean
    public OrdenService ordenService(
            OrdenRepository ordenRepository,
            TransaccionRepository transaccionRepository,
            EmparejamientoService emparejamientoService,
            PortafolioService portafolioService
    ) {
        return new OrdenService(
                ordenRepository,
                transaccionRepository,
                emparejamientoService,
                portafolioService
        );
    }
}
