package com.g1.trading.config;

import com.g1.trading.mercado.aplicacion.InstrumentoFactory;
import com.g1.trading.mercado.aplicacion.InstrumentoService;
import com.g1.trading.mercado.aplicacion.RepositorioInstrumentosFalso;
import com.g1.trading.mercado.dominio.SimulacionPreciosService;
import com.g1.trading.ordenes.OrdenService;
import com.g1.trading.portafolio.PortafolioRepositoryEnMemoria;
import com.g1.trading.portafolio.PortafolioService;
import com.g1.trading.portafolio.VentaNoPermitidaException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

    @Test
    void authControllerAutenticaUsuarioValidoYRechazaCredencialesInvalidas() {
        AuthController controller = new AuthController();

        ResponseEntity<AuthController.UsuarioSesionResponse> ok = controller.login(
                new AuthController.LoginRequest("INV1", "1234")
        );
        assertEquals(HttpStatus.OK, ok.getStatusCode());
        assertNotNull(ok.getBody());
        assertEquals("inv1", ok.getBody().username());
        assertEquals("INV-01", ok.getBody().inversionistaId());
        assertNotNull(ok.getBody().token());

        ResponseEntity<AuthController.UsuarioSesionResponse> noAutorizado = controller.login(
                new AuthController.LoginRequest("inv1", "clave-erronea")
        );
        assertEquals(HttpStatus.UNAUTHORIZED, noAutorizado.getStatusCode());

        assertThrows(
                IllegalArgumentException.class,
                () -> controller.login(new AuthController.LoginRequest(null, "1234"))
        );
    }

    @Test
    void globalExceptionHandlerMapeaErroresDeValidacionYConflictoDeNegocio() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> badRequest = handler.manejarArgumentoInvalido(
                new IllegalArgumentException("Parámetro inválido")
        );
        assertEquals(HttpStatus.BAD_REQUEST, badRequest.getStatusCode());
        assertNotNull(badRequest.getBody());
        assertEquals(400, badRequest.getBody().status());
        assertEquals("Parámetro inválido", badRequest.getBody().mensaje());

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> conflicto = handler.manejarConflictoNegocio(
                new VentaNoPermitidaException("Sin tenencia suficiente")
        );
        assertEquals(HttpStatus.CONFLICT, conflicto.getStatusCode());
        assertNotNull(conflicto.getBody());
        assertEquals(409, conflicto.getBody().status());
    }

    @Test
    void tradingConfigConstruyeOrdenServiceOperativo() {
        TradingConfig config = new TradingConfig();
        PortafolioService portafolioService = new PortafolioService(new PortafolioRepositoryEnMemoria());

        OrdenService ordenService = config.ordenService(
                config.ordenRepository(),
                config.transaccionRepository(),
                config.emparejamientoService(),
                portafolioService
        );

        assertNotNull(ordenService);
        assertTrue(ordenService.listarOrdenesPorInversionista("INV-01").isEmpty());
    }

    @Test
    void dataSeederPrecargaCatalogoYPosicionesDeFormaIdempotente() {
        InstrumentoService instrumentoService = new InstrumentoService(
                new RepositorioInstrumentosFalso(),
                new InstrumentoFactory(),
                new SimulacionPreciosService()
        );
        PortafolioService portafolioService = new PortafolioService(new PortafolioRepositoryEnMemoria());
        DataSeeder seeder = new DataSeeder(instrumentoService, portafolioService);

        seeder.run();
        assertEquals(3, instrumentoService.listarConCotizacionActual().size());
        assertDoesNotThrow(() -> portafolioService.validarVenta("INV-01", "AAPL", new BigDecimal("50")));

        // Segunda ejecución no duplica registros
        seeder.run();
        assertEquals(3, instrumentoService.listarConCotizacionActual().size());
    }
}
