package com.g1.portafolio.infraestructura;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MercadoHttpClient {

    private final RestClient restClient;

    public MercadoHttpClient(@Value("${servicios.mercado.url}") String mercadoUrl) {
        this.restClient = RestClient.builder().baseUrl(mercadoUrl).build();
    }

    private record InstrumentoDto(String id, BigDecimal precioActual) {}

    public Map<String, BigDecimal> obtenerPreciosActuales() {
        Map<String, BigDecimal> precios = new HashMap<>();
        try {
            List<InstrumentoDto> lista = restClient.get()
                    .uri("/api/mercado/instrumentos")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            if (lista != null) {
                lista.forEach(inst -> precios.put(inst.id(), inst.precioActual()));
            }
        } catch (Exception ignored) {
            // Fallback: Portafolio usará costoPromedio si mercado-service no está disponible
        }
        return precios;
    }

    public boolean existeInstrumento(String simbolo) {
        try {
            restClient.get()
                    .uri("/api/mercado/instrumentos/{id}", simbolo)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
