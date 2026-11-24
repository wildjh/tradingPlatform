package com.g1.trading.mercado.aplicacion;

import com.g1.trading.mercado.dominio.Instrumento;
import com.g1.trading.mercado.dominio.OrigenCotizacion;
import com.g1.trading.mercado.dominio.SimulacionPreciosService;
import com.g1.trading.mercado.dominio.TipoInstrumento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * El caso de uso funciona con cualquier objeto que cumpla RepositorioInstrumentos.
 * Aquí ese objeto es un Map. No hay Spring, ni Mockito, ni H2.
 */
class InstrumentoServiceConFalsoTest {

    private final RepositorioInstrumentosFalso repositorio = new RepositorioInstrumentosFalso();
    private final InstrumentoService servicio = new InstrumentoService(
            repositorio, new InstrumentoFactory(), new SimulacionPreciosService());

    @Test
    void registraConsultaYSimulaElCatalogoSinInfraestructura() {
        Instant apertura = Instant.parse("2026-10-06T13:00:00Z");
        Instrumento apple = servicio.registrar(
                "AAPL", "Apple", TipoInstrumento.ACCION, new BigDecimal("100"), apertura, "admin-mercado");

        assertThat(apple.id().valor()).isEqualTo("AAPL");
        assertThat(servicio.listarConCotizacionActual()).containsExactly(apple);
        assertThat(servicio.consultarHistorico("aapl").cotizaciones()).hasSize(1);

        servicio.simularPrecios(new BigDecimal("1.02"), apertura.plusSeconds(60), "admin-mercado");

        Instrumento actualizado = servicio.listarConCotizacionActual().get(0);
        assertThat(actualizado.obtenerCotizacionActual().precio().valor()).isEqualByComparingTo("102.0000");
        assertThat(actualizado.obtenerCotizacionActual().origen()).isEqualTo(OrigenCotizacion.SIMULACION);
        assertThat(servicio.consultarHistorico("AAPL").cotizaciones()).hasSize(2);
    }

    @Test
    void noRegistraDosVecesElMismoSimbolo() {
        Instant apertura = Instant.parse("2026-10-06T13:00:00Z");
        servicio.registrar("AAPL", "Apple", TipoInstrumento.ACCION, new BigDecimal("100"), apertura, "admin-mercado");

        assertThatThrownBy(() -> servicio.registrar(
                "aapl", "Apple Inc.", TipoInstrumento.ACCION, new BigDecimal("101"), apertura.plusSeconds(1), "admin-mercado"))
                .isInstanceOf(SimboloYaRegistradoException.class);

        assertThat(repositorio.listarTodos()).hasSize(1);
    }
}
