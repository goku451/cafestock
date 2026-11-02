package com.dss.cafestock.config;

import com.dss.cafestock.model.Insumo;
import com.dss.cafestock.model.Producto;
import com.dss.cafestock.model.Usuario;
import com.dss.cafestock.repository.InsumoRepository;
import com.dss.cafestock.repository.ProductoRepository;
import com.dss.cafestock.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        inicializarUsuarios();
        inicializarInsumosYProductos();
    }

    private void inicializarUsuarios() {
        // 1. Verificar y migrar usuarios existentes para encriptar contraseñas
        List<Usuario> usuarios = usuarioRepository.findAll();
        for (Usuario u : usuarios) {
            String pass = u.getContrasena();
            if (pass != null && !pass.startsWith("$2a$") && !pass.startsWith("$2b$") && !pass.startsWith("$2y$")) {
                u.setContrasena(passwordEncoder.encode(pass));
                usuarioRepository.save(u);
                System.out.println(">>> [SEGURIDAD] Contraseña encriptada con BCrypt para usuario: " + u.getCorreo());
            }
        }

        // 2. Si no existen los usuarios por defecto, crearlos con contraseñas encriptadas
        if (usuarioRepository.findByCorreo("admin@cafestock.com").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setNombre("Admin");
            admin.setCorreo("admin@cafestock.com");
            admin.setContrasena(passwordEncoder.encode("admin123"));
            admin.setRol("admin");
            usuarioRepository.save(admin);
            System.out.println(">>> [SEGURIDAD] Usuario Admin creado con contraseña BCrypt.");
        }

        if (usuarioRepository.findByCorreo("juan@cafestock.com").isEmpty()) {
            Usuario barista = new Usuario();
            barista.setNombre("Barista Juan");
            barista.setCorreo("juan@cafestock.com");
            barista.setContrasena(passwordEncoder.encode("barista123"));
            barista.setRol("barista");
            usuarioRepository.save(barista);
            System.out.println(">>> [SEGURIDAD] Usuario Barista creado con contraseña BCrypt.");
        }
    }

    private void inicializarInsumosYProductos() {
        if (insumoRepository.count() == 0) {
            Insumo cafe = new Insumo();
            cafe.setNombre("Café en grano");
            cafe.setUnidadMedida("kg");
            cafe.setStockActual(10.0);
            cafe.setStockMinimo(2.0);
            cafe = insumoRepository.save(cafe);

            Insumo leche = new Insumo();
            leche.setNombre("Leche entera");
            leche.setUnidadMedida("L");
            leche.setStockActual(15.0);
            leche.setStockMinimo(5.0);
            leche = insumoRepository.save(leche);

            Insumo croissant = new Insumo();
            croissant.setNombre("Croissant");
            croissant.setUnidadMedida("unid");
            croissant.setStockActual(8.0);
            croissant.setStockMinimo(3.0);
            croissant = insumoRepository.save(croissant);

            Insumo caramelo = new Insumo();
            caramelo.setNombre("Jarabe Caramelo");
            caramelo.setUnidadMedida("L");
            caramelo.setStockActual(5.0);
            caramelo.setStockMinimo(1.0);
            caramelo = insumoRepository.save(caramelo);

            // Crear productos iniciales
            Producto p1 = new Producto();
            p1.setNombre("Café Americano");
            p1.setCategoria("Bebidas Calientes");
            p1.setPrecio(1.75);
            p1.setInsumo(cafe);
            p1.setEstado("Disponible");
            productoRepository.save(p1);

            Producto p2 = new Producto();
            p2.setNombre("Frappé de Caramelo");
            p2.setCategoria("Bebidas Frías");
            p2.setPrecio(3.00);
            p2.setInsumo(caramelo);
            p2.setEstado("Disponible");
            productoRepository.save(p2);

            Producto p3 = new Producto();
            p3.setNombre("Croissant Jamón & Queso");
            p3.setCategoria("Postres y Panadería");
            p3.setPrecio(2.50);
            p3.setInsumo(croissant);
            p3.setEstado("Disponible");
            productoRepository.save(p3);

            Producto p4 = new Producto();
            p4.setNombre("Café Latte");
            p4.setCategoria("Bebidas Calientes");
            p4.setPrecio(2.25);
            p4.setInsumo(leche);
            p4.setEstado("Disponible");
            productoRepository.save(p4);
        }
    }
}
