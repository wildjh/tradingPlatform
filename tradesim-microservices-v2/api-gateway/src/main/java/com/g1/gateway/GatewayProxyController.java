package com.g1.gateway;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import java.net.URI;

@RestController
@RequestMapping("/api")
public class GatewayProxyController {

    private final RestClient restClient;
    private final String mercadoUrl;
    private final String portafolioUrl;
    private final String ordenesUrl;

    public GatewayProxyController(
            @Value("${servicios.mercado.url}") String mercadoUrl,
            @Value("${servicios.portafolio.url}") String portafolioUrl,
            @Value("${servicios.ordenes.url}") String ordenesUrl
    ) {
        this.restClient = RestClient.create();
        this.mercadoUrl = mercadoUrl;
        this.portafolioUrl = portafolioUrl;
        this.ordenesUrl = ordenesUrl;
    }

    @RequestMapping("/**")
    public ResponseEntity<byte[]> enrutar(HttpServletRequest request, @RequestBody(required = false) byte[] body) {
        String path = request.getRequestURI();
        String query = request.getQueryString();
        String targetBase = resolverDestino(path);

        if (targetBase == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"mensaje\":\"Ruta no configurada en API Gateway\"}".getBytes());
        }

        String targetUrl = targetBase + path + (query != null ? "?" + query : "");
        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        try {
            RestClient.RequestBodySpec spec = restClient.method(method)
                    .uri(URI.create(targetUrl))
                    .contentType(MediaType.APPLICATION_JSON);

            if (body != null && body.length > 0) {
                spec.body(body);
            }

            ResponseEntity<byte[]> response = spec.retrieve().toEntity(byte[].class);
            HttpHeaders headers = new HttpHeaders();
            if (response.getHeaders().getContentType() != null) {
                headers.setContentType(response.getHeaders().getContentType());
            }
            return new ResponseEntity<>(response.getBody(), headers, response.getStatusCode());
        } catch (HttpStatusCodeException ex) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(ex.getResponseBodyAsByteArray(), headers, ex.getStatusCode());
        } catch (Exception ex) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String msg = "{\"mensaje\":\"Servicio temporalmente no disponible: " + ex.getMessage().replace("\"", "'") + "\"}";
            return new ResponseEntity<>(msg.getBytes(), headers, HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private String resolverDestino(String path) {
        if (path.startsWith("/api/auth") || path.startsWith("/api/mercado")) {
            return mercadoUrl;
        }
        if (path.startsWith("/api/portafolio")) {
            return portafolioUrl;
        }
        if (path.startsWith("/api/ordenes")) {
            return ordenesUrl;
        }
        return null;
    }
}
