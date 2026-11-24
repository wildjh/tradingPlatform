package com.g1.ordenes.config;

import com.g1.ordenes.dominio.EmparejamientoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrdenesConfig {

    @Bean
    public EmparejamientoService emparejamientoService() {
        return new EmparejamientoService();
    }
}
