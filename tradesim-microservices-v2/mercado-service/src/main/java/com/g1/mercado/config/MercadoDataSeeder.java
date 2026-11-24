package com.g1.mercado.config;

import com.g1.mercado.dominio.OrigenCotizacion;
import com.g1.mercado.dominio.TipoInstrumento;
import com.g1.mercado.persistencia.CotizacionEntity;
import com.g1.mercado.persistencia.CotizacionJpaRepository;
import com.g1.mercado.persistencia.InstrumentoEntity;
import com.g1.mercado.persistencia.InstrumentoJpaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Component
public class MercadoDataSeeder implements CommandLineRunner {

    private final InstrumentoJpaRepository instrumentoRepo;
    private final CotizacionJpaRepository cotizacionRepo;

    public MercadoDataSeeder(InstrumentoJpaRepository instrumentoRepo, CotizacionJpaRepository cotizacionRepo) {
        this.instrumentoRepo = instrumentoRepo;
        this.cotizacionRepo = cotizacionRepo;
    }

    @Override
    public void run(String... args) {
        if (instrumentoRepo.count() == 0) {
            registrar("AAPL", "Apple Inc.", TipoInstrumento.ACCION, new BigDecimal("185.50"));
            registrar("MSFT", "Microsoft Corp.", TipoInstrumento.ACCION, new BigDecimal("420.00"));
            registrar("SPY", "SPDR S&P 500 ETF", TipoInstrumento.ETF, new BigDecimal("510.25"));
        }
    }

    private void registrar(String id, String nombre, TipoInstrumento tipo, BigDecimal precio) {
        instrumentoRepo.save(new InstrumentoEntity(id, nombre, tipo));
        cotizacionRepo.save(new CotizacionEntity(id, precio, Instant.now(), OrigenCotizacion.CARGA_INICIAL, "SYSTEM_SEED"));
    }
}
