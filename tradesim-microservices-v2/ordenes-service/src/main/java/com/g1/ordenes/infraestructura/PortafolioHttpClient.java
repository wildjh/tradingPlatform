package com.g1.ordenes.infraestructura;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
public class PortafolioHttpClient {

    private final RestClient restClient;

    public PortafolioHttpClient(@Value("${servicios.portafolio.url}") String portafolioUrl) {
        this.restClient = RestClient.builder().baseUrl(portafolioUrl).build();
    }

    private record ValidarVentaRequest(String inversionistaId, String instrumentoId, BigDecimal cantidad) {}
    private record AplicarTransaccionRequest(String compradorId, String vendedorId, String instrumentoId, BigDecimal cantidad, BigDecimal precio) {}
    private record ErrorBody(String mensaje) {}

    public void validarVenta(String inversionistaId, String instrumentoId, BigDecimal cantidad) {
        try {
            restClient.post()
                    .uri("/api/portafolio/validar-venta")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ValidarVentaRequest(inversionistaId, instrumentoId, cantidad))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException ex) {
            throw new IllegalStateException(extraerMensaje(ex));
        }
    }

    public void aplicarTransaccion(String compradorId, String vendedorId, String instrumentoId, BigDecimal cantidad, BigDecimal precio) {
        try {
            restClient.post()
                    .uri("/api/portafolio/transacciones")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new AplicarTransaccionRequest(compradorId, vendedorId, instrumentoId, cantidad, precio))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException ex) {
            throw new IllegalStateException(extraerMensaje(ex));
        }
    }

    private String extraerMensaje(HttpClientErrorException ex) {
        try {
            ErrorBody error = ex.getResponseBodyAs(ErrorBody.class);
            if (error != null && error.mensaje() != null && !error.mensaje().isBlank()) {
                return error.mensaje();
            }
        } catch (Exception ignored) {}
        return ex.getResponseBodyAsString();
    }
}