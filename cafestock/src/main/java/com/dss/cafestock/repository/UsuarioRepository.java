package com.dss.cafestock.repository;

import com.dss.cafestock.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByCorreoAndContrasena(String correo, String contrasena);
    Optional<Usuario> findByCorreo(String correo);
}

