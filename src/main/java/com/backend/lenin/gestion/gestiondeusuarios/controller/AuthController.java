package com.backend.lenin.gestion.gestiondeusuarios.controller;

import com.backend.lenin.gestion.gestiondeusuarios.dto.LoginRequest;
import com.backend.lenin.gestion.gestiondeusuarios.dto.UsuarioRequest;
import com.backend.lenin.gestion.gestiondeusuarios.model.Administrador;
import com.backend.lenin.gestion.gestiondeusuarios.model.Usuario;
import com.backend.lenin.gestion.gestiondeusuarios.security.JwtUtil;
import com.backend.lenin.gestion.gestiondeusuarios.service.AdministradorService;
import com.backend.lenin.gestion.gestiondeusuarios.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AdministradorService administradorService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Administrador admin = administradorService.buscarPorCorreo(request.getCorreo()).orElse(null);
        if (admin == null || !passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));
        }

        String token = jwtUtil.generateToken(admin.getCorreo());
        return ResponseEntity.ok(Map.of(
                "token", token,
                "admin", admin
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UsuarioRequest nuevo, HttpServletRequest request) {
        String correoAdmin = (String) request.getAttribute("correo");

        if (correoAdmin == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Token inválido o ausente"));
        }

        Administrador admin = administradorService.buscarPorCorreo(correoAdmin).orElse(null);
        if (admin == null) {
            return ResponseEntity.status(403).body(Map.of("error", "No autorizado"));
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(nuevo.getNombre());
        nuevoUsuario.setCorreo(nuevo.getCorreo());
        nuevoUsuario.setPassword(passwordEncoder.encode(nuevo.getPassword()));

usuarioService.registrarUsuario(nuevoUsuario);
        return ResponseEntity.ok(Map.of("mensaje", "Usuario registrado correctamente"));
    }
}
