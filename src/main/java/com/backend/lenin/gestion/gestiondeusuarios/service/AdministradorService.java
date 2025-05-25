package com.backend.lenin.gestion.gestiondeusuarios.service;

import com.backend.lenin.gestion.gestiondeusuarios.model.Administrador;
import com.backend.lenin.gestion.gestiondeusuarios.repository.AdministradorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdministradorService {

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Administrador registrarAdministrador(Administrador administrador) {
        administrador.setPassword(passwordEncoder.encode(administrador.getPassword()));
        return administradorRepository.save(administrador);
    }

    public Optional<Administrador> buscarPorCorreo(String correo) {
        return administradorRepository.findByCorreo(correo);
    }
}
