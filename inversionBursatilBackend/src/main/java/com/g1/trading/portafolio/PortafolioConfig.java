package com.g1.trading.portafolio;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Expone {@link PortafolioService} como bean.
 * El servicio no lleva anotaciones de Spring; el repositorio sí (adaptador JPA).
 */
@Configuration
public class PortafolioConfig {

    @Bean
    public PortafolioService portafolioService(PortafolioRepository repositorio) {
        return new PortafolioService(repositorio);
    }
}
