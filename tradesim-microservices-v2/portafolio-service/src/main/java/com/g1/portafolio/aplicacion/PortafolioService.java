package com.g1.portafolio.aplicacion;

import com.g1.portafolio.dominio.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public final class PortafolioService {

    private final PortafolioRepository repositorio;
    private final ValidacionVentaService validacionVenta = new ValidacionVentaService();

    public PortafolioService(PortafolioRepository repositorio) {
        if (repositorio == null) throw new IllegalArgumentException("Se necesita un repositorio de portafolios");
        this.repositorio = repositorio;
    }

    public synchronized void cargarPosicionInicial(String inversionistaId, String instrumentoId,
                                                   BigDecimal cantidad, BigDecimal precio) {
        Portafolio portafolio = obtenerOCrear(inversionistaId);
        portafolio.registrarCompra(instrumento(instrumentoId), Cantidad.de(cantidad), precio);
        repositorio.guardar(portafolio);
    }

    public synchronized void validarVenta(String inversionistaId, String instrumentoId, BigDecimal cantidad) {
        Portafolio portafolio = obtenerOCrear(inversionistaId);
        validacionVenta.validar(portafolio, instrumento(instrumentoId), Cantidad.de(cantidad));
    }

    public synchronized void aplicarTransaccion(String compradorId, String vendedorId, String instrumentoId,
                                                BigDecimal cantidad, BigDecimal precio) {
        InstrumentoId instrumento = instrumento(instrumentoId);
        Cantidad unidades = Cantidad.de(cantidad);
        Portafolio vendedor = obtenerOCrear(vendedorId);
        Portafolio comprador = obtenerOCrear(compradorId);
        validacionVenta.validar(vendedor, instrumento, unidades);

        vendedor.registrarVenta(instrumento, unidades, precio);
        comprador.registrarCompra(instrumento, unidades, precio);
        repositorio.guardar(vendedor);
        repositorio.guardar(comprador);
    }

    public synchronized ResumenPortafolio consultar(String inversionistaId, Map<String, BigDecimal> preciosPorSimbolo) {
        Portafolio portafolio = obtenerOCrear(inversionistaId);
        Map<InstrumentoId, BigDecimal> precios = new HashMap<>();
        if (preciosPorSimbolo != null) {
            preciosPorSimbolo.forEach((simbolo, precio) -> precios.put(instrumento(simbolo), precio));
        }
        return new ResumenPortafolio(portafolio.inversionistaId(), portafolio.posiciones(),
                portafolio.valorTotal(precios), portafolio.gananciaNoRealizada(precios));
    }

    private Portafolio obtenerOCrear(String inversionistaId) {
        if (inversionistaId == null || inversionistaId.isBlank()) {
            throw new IllegalArgumentException("Se necesita el id del inversionista");
        }
        InversionistaId id = new InversionistaId(inversionistaId.trim());
        return repositorio.buscarPorInversionista(id).orElseGet(() -> {
            Portafolio nuevo = PortafolioFactory.crearVacio(id.valor());
            repositorio.guardar(nuevo);
            return nuevo;
        });
    }

    private static InstrumentoId instrumento(String simbolo) {
        if (simbolo == null || simbolo.isBlank()) {
            throw new IllegalArgumentException("Se necesita el id del instrumento");
        }
        return new InstrumentoId(simbolo.trim().toUpperCase());
    }
}
