package com.g1.portafolio.config;

import com.g1.portafolio.aplicacion.PortafolioService;
import com.g1.portafolio.dominio.PortafolioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PortafolioConfig {

    @Bean
    public PortafolioService portafolioService(PortafolioRepository portafolioRepository) {
        return new PortafolioService(portafolioRepository);
    }
}
