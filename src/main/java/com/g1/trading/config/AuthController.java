package com.g1.trading.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record LoginRequest(String username, String password) {}

    public record UsuarioSesionResponse(
            String token,
            String username,
            String nombreCompleto,
            String rol,
            String inversionistaId
    ) {}

    private record UsuarioPrecargado(
            String username,
            String password,
            String nombreCompleto,
            String rol,
            String inversionistaId
    ) {}

    private static final Map<String, UsuarioPrecargado> USUARIOS = Map.of(
            "inv1", new UsuarioPrecargado("inv1", "1234", "Carlos Mendoza", "INVERSIONISTA", "INV-01"),
            "inv2", new UsuarioPrecargado("inv2", "1234", "Laura Gómez", "INVERSIONISTA", "INV-02"),
            "inv3", new UsuarioPrecargado("inv3", "1234", "Andrés Ruiz", "INVERSIONISTA", "INV-03"),
            "admin", new UsuarioPrecargado("admin", "admin123", "Administrador G1", "ADMIN", null)
    );

    @PostMapping("/login")
    public ResponseEntity<UsuarioSesionResponse> login(@RequestBody LoginRequest req) {
        if (req.username() == null || req.password() == null) {
            throw new IllegalArgumentException("Usuario y contraseña obligatorios");
        }

        UsuarioPrecargado user = USUARIOS.get(req.username().trim().toLowerCase());
        if (user == null || !user.password().equals(req.password())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(new UsuarioSesionResponse(
                UUID.randomUUID().toString(),
                user.username(),
                user.nombreCompleto(),
                user.rol(),
                user.inversionistaId()
        ));
    }
}