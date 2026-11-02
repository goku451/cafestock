CREATE DATABASE IF NOT EXISTS cafestock;
USE cafestock;

-- =========================================================================
-- CafeStock Database Initialization
-- Las tablas principales y relaciones son gestionadas y actualizadas
-- automáticamente por Hibernate / Spring Data JPA (ddl-auto=update).
-- =========================================================================

-- Datos iniciales de usuarios
-- NOTA DE SEGURIDAD:
-- Las contraseñas están encriptadas con BCrypt ($2a$10$...).
-- admin@cafestock.com -> admin123
-- juan@cafestock.com  -> barista123
-- DataInitializer encriptará automáticamente cualquier contraseña en texto plano al iniciar el servidor.
INSERT INTO usuario (id_usuario, nombre, correo, contrasena, rol) VALUES 
(1, 'Admin', 'admin@cafestock.com', '$2a$10$BshK1xQ79BqjP7wV6aOQn.U1eUf8m3B3/XvQYd3Z3o9qB2fEsaCym', 'admin'),
(2, 'Barista Juan', 'juan@cafestock.com', '$2a$10$BshK1xQ79BqjP7wV6aOQn.U1eUf8m3B3/XvQYd3Z3o9qB2fEsaCym', 'barista')
ON DUPLICATE KEY UPDATE id_usuario=id_usuario;

-- Datos iniciales de insumos
INSERT INTO insumo (id_insumo, nombre, unidad_medida, stock_actual, stock_minimo) VALUES 
(1, 'Café en grano', 'kg', 10.0, 2.0),
(2, 'Leche entera', 'L', 15.0, 5.0),
(3, 'Croissant', 'unid', 8.0, 3.0),
(4, 'Jarabe Caramelo', 'L', 5.0, 1.0)
ON DUPLICATE KEY UPDATE id_insumo=id_insumo;

-- Datos iniciales de productos
INSERT INTO producto (id_producto, nombre, categoria, precio, estado, id_insumo) VALUES 
(1, 'Café Americano', 'Bebidas Calientes', 1.75, 'Disponible', 1),
(2, 'Frappé de Caramelo', 'Bebidas Frías', 3.00, 'Disponible', 4),
(3, 'Croissant Jamón & Queso', 'Postres', 2.50, 'Disponible', 3),
(4, 'Café Latte', 'Bebidas Calientes', 2.25, 'Disponible', 2)
ON DUPLICATE KEY UPDATE id_producto=id_producto;
