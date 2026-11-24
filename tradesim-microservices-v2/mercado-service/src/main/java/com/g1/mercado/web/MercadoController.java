package com.g1.mercado.web;

import com.g1.mercado.dominio.OrigenCotizacion;
import com.g1.mercado.dominio.TipoInstrumento;
import com.g1.mercado.persistencia.CotizacionEntity;
import com.g1.mercado.persistencia.CotizacionJpaRepository;
import com.g1.mercado.persistencia.InstrumentoEntity;
import com.g1.mercado.persistencia.InstrumentoJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class MercadoController {

    private final InstrumentoJpaRepository instrumentoRepo;
    private final CotizacionJpaRepository cotizacionRepo;

    public MercadoController(InstrumentoJpaRepository instrumentoRepo, CotizacionJpaRepository cotizacionRepo) {
        this.instrumentoRepo = instrumentoRepo;
        this.cotizacionRepo = cotizacionRepo;
    }

    public record InstrumentoResumenResponse(
            String id,
            String nombre,
            TipoInstrumento tipo,
            BigDecimal precioActual,
            Instant ultimaActualizacion
    ) {}

    public record RegistrarInstrumentoRequest(
            String id,
            String nombre,
            TipoInstrumento tipo,
            BigDecimal precioInicial,
            String adminId
    ) {}

    public record ActualizarPrecioRequest(
            String instrumentoId,
            BigDecimal nuevoPrecio,
            String adminId
    ) {}

    public record LoginRequest(String username, String password) {}
    public record UsuarioSesionResponse(String token, String username, String nombreCompleto, String rol, String inversionistaId) {}
    private record UsuarioPrecargado(String username, String password, String nombreCompleto, String rol, String inversionistaId) {}

    private static final Map<String, UsuarioPrecargado> USUARIOS = Map.of(
            "inv1", new UsuarioPrecargado("inv1", "1234", "Carlos Mendoza", "INVERSIONISTA", "INV-01"),
            "inv2", new UsuarioPrecargado("inv2", "1234", "Laura Gómez", "INVERSIONISTA", "INV-02"),
            "inv3", new UsuarioPrecargado("inv3", "1234", "Andrés Ruiz", "INVERSIONISTA", "INV-03"),
            "admin", new UsuarioPrecargado("admin", "admin123", "Administrador G1", "ADMIN", null)
    );

    @PostMapping("/auth/login")
    public ResponseEntity<UsuarioSesionResponse> login(@RequestBody LoginRequest req) {
        if (req.username() == null || req.password() == null) {
            throw new IllegalArgumentException("Usuario y contraseña obligatorios");
        }
        UsuarioPrecargado user = USUARIOS.get(req.username().trim().toLowerCase());
        if (user == null || !user.password().equals(req.password())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(new UsuarioSesionResponse(
                UUID.randomUUID().toString(), user.username(), user.nombreCompleto(), user.rol(), user.inversionistaId()
        ));
    }

    @GetMapping("/mercado/instrumentos")
    public ResponseEntity<List<InstrumentoResumenResponse>> listarInstrumentos() {
        List<InstrumentoResumenResponse> lista = instrumentoRepo.findAll().stream()
                .map(inst -> {
                    CotizacionEntity cot = cotizacionRepo.findFirstByInstrumentoIdOrderByMomentoDesc(inst.getId())
                            .orElseThrow(() -> new IllegalStateException("Sin cotización para " + inst.getId()));
                    return new InstrumentoResumenResponse(inst.getId(), inst.getNombre(), inst.getTipo(), cot.getPrecio(), cot.getMomento());
                })
                .toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/mercado/instrumentos/{id}")
    public ResponseEntity<InstrumentoResumenResponse> obtenerInstrumento(@PathVariable String id) {
        String simbolo = id.trim().toUpperCase();
        InstrumentoEntity inst = instrumentoRepo.findById(simbolo)
                .orElseThrow(() -> new IllegalArgumentException("Instrumento no encontrado: " + simbolo));
        CotizacionEntity cot = cotizacionRepo.findFirstByInstrumentoIdOrderByMomentoDesc(simbolo)
                .orElseThrow(() -> new IllegalStateException("Sin cotización para " + simbolo));
        return ResponseEntity.ok(new InstrumentoResumenResponse(inst.getId(), inst.getNombre(), inst.getTipo(), cot.getPrecio(), cot.getMomento()));
    }

    // Ruta exacta que consume api.ts: /mercado/instrumentos/{id}/historial
    @GetMapping("/mercado/instrumentos/{id}/historial")
    public ResponseEntity<List<CotizacionEntity>> obtenerHistorial(@PathVariable String id) {
        return ResponseEntity.ok(cotizacionRepo.findByInstrumentoIdOrderByMomentoAsc(id.trim().toUpperCase()));
    }

    @PostMapping("/mercado/instrumentos")
    public ResponseEntity<InstrumentoResumenResponse> registrarInstrumento(@RequestBody RegistrarInstrumentoRequest req) {
        if (req.id() == null || req.id().isBlank()) {
            throw new IllegalArgumentException("El símbolo del instrumento es obligatorio");
        }
        String simbolo = req.id().trim().toUpperCase();
        if (!simbolo.matches("[A-Z]{1,5}")) {
            throw new IllegalArgumentException("El símbolo debe tener de 1 a 5 letras");
        }
        if (instrumentoRepo.existsById(simbolo)) {
            throw new IllegalArgumentException("El instrumento ya existe: " + simbolo);
        }
        if (req.nombre() == null || req.nombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del instrumento es obligatorio");
        }
        if (req.precioInicial() == null || req.precioInicial().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio inicial debe ser mayor a cero");
        }

        Instant ahora = Instant.now();
        String actor = (req.adminId() != null && !req.adminId().isBlank()) ? req.adminId().trim() : "ADMIN-01";
        BigDecimal precio = req.precioInicial().setScale(4, RoundingMode.HALF_UP);

        instrumentoRepo.save(new InstrumentoEntity(simbolo, req.nombre().trim(), req.tipo()));
        cotizacionRepo.save(new CotizacionEntity(simbolo, precio, ahora, OrigenCotizacion.CARGA_INICIAL, actor));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new InstrumentoResumenResponse(simbolo, req.nombre().trim(), req.tipo(), precio, ahora));
    }

    // Ruta exacta que consume api.ts: POST /mercado/cotizaciones
    @PostMapping("/mercado/cotizaciones")
    public ResponseEntity<CotizacionEntity> actualizarPrecio(@RequestBody ActualizarPrecioRequest req) {
        if (req.instrumentoId() == null || req.instrumentoId().isBlank()) {
            throw new IllegalArgumentException("El instrumentoId es obligatorio");
        }
        String simbolo = req.instrumentoId().trim().toUpperCase();
        if (!instrumentoRepo.existsById(simbolo)) {
            throw new IllegalArgumentException("Instrumento no encontrado: " + simbolo);
        }
        if (req.nuevoPrecio() == null || req.nuevoPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El nuevo precio debe ser mayor a cero");
        }

        String actor = (req.adminId() != null && !req.adminId().isBlank()) ? req.adminId().trim() : "ADMIN-01";
        BigDecimal precio = req.nuevoPrecio().setScale(4, RoundingMode.HALF_UP);
        CotizacionEntity nueva = new CotizacionEntity(simbolo, precio, Instant.now(), OrigenCotizacion.SIMULACION, actor);

        return ResponseEntity.status(HttpStatus.CREATED).body(cotizacionRepo.save(nueva));
    }
}
