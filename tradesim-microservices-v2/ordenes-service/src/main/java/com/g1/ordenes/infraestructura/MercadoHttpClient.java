package com.g1.ordenes.infraestructura;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
public class MercadoHttpClient {

    private final RestClient restClient;

    public MercadoHttpClient(@Value("${servicios.mercado.url}") String mercadoUrl) {
        this.restClient = RestClient.builder().baseUrl(mercadoUrl).build();
    }

    private record InstrumentoDto(String id, BigDecimal precioActual) {}

    public BigDecimal obtenerPrecioActual(String instrumentoId) {
        try {
            InstrumentoDto dto = restClient.get()
                    .uri("/api/mercado/instrumentos/{id}", instrumentoId)
                    .retrieve()
                    .body(InstrumentoDto.class);
            return dto != null ? dto.precioActual() : null;
        } catch (Exception ex) {
            return null;
        }
    }
}
