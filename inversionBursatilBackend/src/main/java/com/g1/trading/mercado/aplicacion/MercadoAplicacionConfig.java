package com.g1.trading.mercado.aplicacion;

import com.g1.trading.mercado.dominio.SimulacionPreciosService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * El servicio de dominio no lleva anotaciones de Spring.
 * Esta configuración, en la capa de aplicación, lo publica como bean.
 */
@Configuration
public class MercadoAplicacionConfig {

    @Bean
    public SimulacionPreciosService simulacionPreciosService() {
        return new SimulacionPreciosService();
    }
}
