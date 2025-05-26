package com.backend.lenin.gestion.gestiondeusuarios.controller;

import com.backend.lenin.gestion.gestiondeusuarios.dto.LoginRequest;
import com.backend.lenin.gestion.gestiondeusuarios.dto.UsuarioRequest;
import com.backend.lenin.gestion.gestiondeusuarios.model.Administrador;
import com.backend.lenin.gestion.gestiondeusuarios.model.Usuario;
import com.backend.lenin.gestion.gestiondeusuarios.security.JwtUtil;
import com.backend.lenin.gestion.gestiondeusuarios.service.AdministradorService;
import com.backend.lenin.gestion.gestiondeusuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "bearerAuth")
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
    @SecurityRequirement(name = "") // este endpoint no requiere token
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Administrador admin = administradorService.buscarPorCorreo(request.getCorreo()).orElse(null);
        if (admin == null || !passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            return ResponseEntity.status(403).body(Map.of("error", "Credenciales inválidas"));
        }
        String token = jwtUtil.generateToken(admin.getCorreo());
        return ResponseEntity.ok(Map.of("token", token, "admin", admin));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UsuarioRequest nuevo, HttpServletRequest request) {
        System.out.println(">> LLEGÓ AL /api/register");

        String correoToken = (String) request.getAttribute("correo");
        System.out.println(">> CORREO EN REQUEST: " + correoToken);

        if (correoToken == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Token inválido o ausente"));
        }

        boolean esAdmin = administradorService.buscarPorCorreo(correoToken).isPresent();
        System.out.println(">> ¿ADMIN EXISTE?: " + esAdmin);

        if (!esAdmin) {
            return ResponseEntity.status(403).body(Map.of("error", "No autorizado"));
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(nuevo.getNombre());
        nuevoUsuario.setCorreo(nuevo.getCorreo());
        nuevoUsuario.setPassword(passwordEncoder.encode(nuevo.getPassword()));

        usuarioService.registrarUsuario(nuevoUsuario);
        return ResponseEntity.ok(Map.of("mensaje", "Usuario registrado correctamente"));
    }

    @GetMapping("/usuarios")
    public ResponseEntity<?> listarUsuarios(HttpServletRequest request) {
        String correo = (String) request.getAttribute("correo");

        if (correo == null || !administradorService.buscarPorCorreo(correo).isPresent()) {
            return ResponseEntity.status(403).body(Map.of("error", "No autorizado"));
        }

        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }
}
