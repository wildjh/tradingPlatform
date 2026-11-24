package com.g1.trading;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Arranque del esqueleto (sección 5).
 * El dominio de Mercado no depende de esta clase: las reglas viven en Java puro.
 */
@SpringBootApplication
public class TradingApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradingApiApplication.class, args);
    }
}
