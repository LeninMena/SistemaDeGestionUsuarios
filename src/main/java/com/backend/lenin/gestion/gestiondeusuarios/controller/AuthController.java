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
import java.util.Optional;

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
            return ResponseEntity.status(403).body(Map.of("error", "Credenciales inválidas"));
        }

        String token = jwtUtil.generateToken(admin.getCorreo());
        return ResponseEntity.ok(Map.of("token", token, "admin", admin));
    }

    @PostMapping("/login-usuario")
    public ResponseEntity<?> loginUsuario(@RequestBody LoginRequest request) {
        String correo = request.getCorreo().trim().toLowerCase();
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorCorreo(correo);

        System.out.println(">> CORREO ENVIADO: " + correo);
        System.out.println(">> ¿EXISTE?: " + usuarioOpt.isPresent());

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(403).body(Map.of("error", "Correo no encontrado"));
        }

        System.out.println(">> PASSWORD ENVIADA: " + request.getPassword());
        System.out.println(">> PASSWORD GUARDADA: " + usuarioOpt.get().getPassword());

        if (!passwordEncoder.matches(request.getPassword(), usuarioOpt.get().getPassword())) {
            return ResponseEntity.status(403).body(Map.of("error", "Contraseña incorrecta"));
        }

        String token = jwtUtil.generateToken(usuarioOpt.get().getCorreo());
        return ResponseEntity.ok(Map.of("token", token, "usuario", usuarioOpt.get()));
    }

    @PostMapping("/register")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> register(@RequestBody UsuarioRequest nuevo, HttpServletRequest request) {
        String correoToken = (String) request.getAttribute("correo");

        if (correoToken == null || !administradorService.buscarPorCorreo(correoToken).isPresent()) {
            return ResponseEntity.status(403).body(Map.of("error", "No autorizado"));
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(nuevo.getNombre());
        nuevoUsuario.setCorreo(nuevo.getCorreo().toLowerCase());
        nuevoUsuario.setPassword(passwordEncoder.encode(nuevo.getPassword()));

        usuarioService.registrarUsuario(nuevoUsuario);
        return ResponseEntity.ok(Map.of("mensaje", "Usuario registrado correctamente"));
    }

    @GetMapping("/usuarios")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> listarUsuarios(HttpServletRequest request) {
        String correo = (String) request.getAttribute("correo");

        if (correo == null || !administradorService.buscarPorCorreo(correo).isPresent()) {
            return ResponseEntity.status(403).body(Map.of("error", "No autorizado"));
        }

        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    // 🔐 Para testear si la contraseña sin encriptar coincide con el hash
    @GetMapping("/test-password")
    public ResponseEntity<?> testPassword(@RequestParam String raw, @RequestParam String hash) {
        boolean matches = passwordEncoder.matches(raw, hash);
        return ResponseEntity.ok(Map.of(
                "rawPassword", raw,
                "hashGuardado", hash,
                "coincide", matches
        ));
    }
}
