package com.backend.lenin.gestion.gestiondeusuarios.repository;

import com.backend.lenin.gestion.gestiondeusuarios.model.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
    Optional<Administrador> findByCorreo(String correo);
}
