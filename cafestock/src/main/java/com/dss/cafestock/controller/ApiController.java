package com.dss.cafestock.controller;

import com.dss.cafestock.model.*;
import com.dss.cafestock.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==========================================
    // 1. AUTENTICACIÓN Y USUARIOS
    // ==========================================

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String correo = credentials.get("correo");
        String contrasena = credentials.get("contrasena");

        if (correo == null || contrasena == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Correo y contraseña son requeridos"));
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            boolean match = false;

            // Verificar si coincide con hash BCrypt
            if (usuario.getContrasena().startsWith("$2a$") || usuario.getContrasena().startsWith("$2b$") || usuario.getContrasena().startsWith("$2y$")) {
                match = passwordEncoder.matches(contrasena, usuario.getContrasena());
            } else if (contrasena.equals(usuario.getContrasena())) {
                // Si la contraseña aún era texto plano, migrarla a BCrypt inmediatamente
                usuario.setContrasena(passwordEncoder.encode(contrasena));
                usuarioRepository.save(usuario);
                match = true;
            }

            if (match) {
                // Devolver objeto seguro sin hash de contraseña
                Map<String, Object> resp = new HashMap<>();
                resp.put("idUsuario", usuario.getIdUsuario());
                resp.put("nombre", usuario.getNombre());
                resp.put("correo", usuario.getCorreo());
                resp.put("rol", usuario.getRol());
                return ResponseEntity.ok(resp);
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Credenciales incorrectas"));
    }

    @GetMapping("/usuarios")
    public List<Map<String, Object>> getUsuarios() {
        List<Usuario> list = usuarioRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Usuario u : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("idUsuario", u.getIdUsuario());
            map.put("nombre", u.getNombre());
            map.put("correo", u.getCorreo());
            map.put("rol", u.getRol());
            result.add(map);
        }
        return result;
    }

    @PostMapping("/usuarios")
    public ResponseEntity<?> createUsuario(@RequestBody Usuario usuario) {
        if (usuario.getCorreo() == null || usuario.getCorreo().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El correo es requerido"));
        }
        if (usuarioRepository.findByCorreo(usuario.getCorreo()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El correo ya está registrado"));
        }
        if (usuario.getContrasena() == null || usuario.getContrasena().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "La contraseña es requerida"));
        }

        // Encriptar contraseña con BCrypt
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        if (usuario.getRol() == null || usuario.getRol().isBlank()) {
            usuario.setRol("barista");
        }
        Usuario saved = usuarioRepository.save(usuario);

        Map<String, Object> resp = new HashMap<>();
        resp.put("idUsuario", saved.getIdUsuario());
        resp.put("nombre", saved.getNombre());
        resp.put("correo", saved.getCorreo());
        resp.put("rol", saved.getRol());
        return ResponseEntity.ok(resp);
    }

    // ==========================================
    // 2. INSUMOS CRUD
    // ==========================================

    @GetMapping("/insumos")
    public List<Insumo> getInsumos() {
        return insumoRepository.findAll();
    }

    @PostMapping("/insumos")
    public ResponseEntity<?> createInsumo(@RequestBody Insumo insumo) {
        if (insumo.getNombre() == null || insumo.getNombre().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El nombre del insumo es obligatorio"));
        }
        if (insumo.getStockActual() == null) insumo.setStockActual(0.0);
        if (insumo.getStockMinimo() == null) insumo.setStockMinimo(1.0);
        if (insumo.getUnidadMedida() == null) insumo.setUnidadMedida("unid");

        Insumo saved = insumoRepository.save(insumo);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/insumos/{id}")
    public ResponseEntity<?> updateInsumo(@PathVariable Long id, @RequestBody Insumo updated) {
        return insumoRepository.findById(id).map(insumo -> {
            insumo.setNombre(updated.getNombre());
            insumo.setUnidadMedida(updated.getUnidadMedida());
            insumo.setStockActual(updated.getStockActual());
            insumo.setStockMinimo(updated.getStockMinimo());
            Insumo saved = insumoRepository.save(insumo);

            // Actualizar estado de los productos vinculados
            List<Producto> productos = productoRepository.findByInsumo_IdInsumo(saved.getIdInsumo());
            for (Producto p : productos) {
                actualizarEstadoProducto(p);
            }

            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/insumos/{id}")
    public ResponseEntity<?> deleteInsumo(@PathVariable Long id) {
        List<Producto> productos = productoRepository.findByInsumo_IdInsumo(id);
        if (!productos.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "No se puede eliminar el insumo porque está vinculado a " + productos.size() + " producto(s)."
            ));
        }
        insumoRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Insumo eliminado correctamente"));
    }

    // ==========================================
    // 3. PRODUCTOS CRUD (MEJORADO PARA ADMIN)
    // ==========================================

    @GetMapping("/productos")
    public List<Producto> getProductos() {
        List<Producto> productos = productoRepository.findAll();
        for (Producto p : productos) {
            actualizarEstadoProducto(p);
        }
        return productos;
    }

    @GetMapping("/productos/{id}")
    public ResponseEntity<?> getProducto(@PathVariable Long id) {
        return productoRepository.findById(id)
                .map(p -> {
                    actualizarEstadoProducto(p);
                    return ResponseEntity.ok(p);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/productos")
    public ResponseEntity<?> createProducto(@RequestBody Producto producto) {
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El nombre del producto es obligatorio"));
        }
        if (producto.getPrecio() == null || producto.getPrecio() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "El precio debe ser mayor a 0"));
        }
        if (producto.getCategoria() == null || producto.getCategoria().isBlank()) {
            producto.setCategoria("Bebidas");
        }

        // Asociar insumo si se proporcionó
        if (producto.getInsumo() != null && producto.getInsumo().getIdInsumo() != null) {
            Insumo insumo = insumoRepository.findById(producto.getInsumo().getIdInsumo()).orElse(null);
            if (insumo == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "El insumo seleccionado no existe"));
            }
            producto.setInsumo(insumo);
            producto.setEstado(insumo.getStockActual() > 0 ? "Disponible" : "Agotado");
        } else {
            producto.setEstado("Disponible");
        }

        Producto saved = productoRepository.save(producto);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/productos/{id}")
    public ResponseEntity<?> updateProducto(@PathVariable Long id, @RequestBody Producto updated) {
        return productoRepository.findById(id).map(producto -> {
            if (updated.getNombre() != null && !updated.getNombre().isBlank()) {
                producto.setNombre(updated.getNombre());
            }
            if (updated.getCategoria() != null && !updated.getCategoria().isBlank()) {
                producto.setCategoria(updated.getCategoria());
            }
            if (updated.getPrecio() != null && updated.getPrecio() > 0) {
                producto.setPrecio(updated.getPrecio());
            }

            if (updated.getInsumo() != null && updated.getInsumo().getIdInsumo() != null) {
                Insumo insumo = insumoRepository.findById(updated.getInsumo().getIdInsumo()).orElse(null);
                producto.setInsumo(insumo);
            }

            actualizarEstadoProducto(producto);
            Producto saved = productoRepository.save(producto);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/productos/{id}")
    public ResponseEntity<?> deleteProducto(@PathVariable Long id) {
        if (!productoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        // Desvincular pedidos existentes para evitar error de llave foránea
        List<Pedido> pedidos = pedidoRepository.findAll();
        for (Pedido ped : pedidos) {
            if (ped.getProducto() != null && ped.getProducto().getIdProducto().equals(id)) {
                ped.setProducto(null);
                pedidoRepository.save(ped);
            }
        }

        productoRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Producto eliminado con éxito"));
    }

    // ==========================================
    // 4. PEDIDOS (ORDENAR Y DESCUENTO DE STOCK)
    // ==========================================

    @PostMapping("/pedidos")
    public ResponseEntity<?> crearPedido(@RequestBody PedidoRequest req) {
        if (req.getIdProducto() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Debe seleccionar un producto"));
        }
        if (req.getCantidad() == null || req.getCantidad() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "La cantidad debe ser mayor a 0"));
        }

        Producto producto = productoRepository.findById(req.getIdProducto()).orElse(null);
        if (producto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Producto no encontrado"));
        }

        Insumo insumo = producto.getInsumo();
        if (insumo == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "El producto no tiene un insumo asociado en inventario"));
        }

        double stockActual = insumo.getStockActual() != null ? insumo.getStockActual() : 0.0;
        int cantidadPedida = req.getCantidad();
        String cliente = (req.getCliente() != null && !req.getCliente().isBlank()) ? req.getCliente().trim() : "Cliente Mostrador";

        // VERIFICAR STOCK
        if (stockActual < cantidadPedida) {
            // NOTIFICAR AL ADMIN: Intento fallido por stock insuficiente
            String alertaMsg = String.format(
                "El cliente '%s' intentó pedir %d unidad(es) de '%s', pero solo hay %.2f %s disponibles de '%s'.",
                cliente, cantidadPedida, producto.getNombre(), stockActual, insumo.getUnidadMedida(), insumo.getNombre()
            );
            Notificacion notif = new Notificacion("⚠️ Pedido Rechazado: Stock Insuficiente", alertaMsg, "STOCK_INSUFICIENTE");
            notificacionRepository.save(notif);

            // Actualizar estado del producto si ya quedó en 0
            if (stockActual <= 0) {
                producto.setEstado("Agotado");
                productoRepository.save(producto);
            }

            Map<String, Object> errorResp = new HashMap<>();
            errorResp.put("success", false);
            errorResp.put("error", "STOCK_INSUFICIENTE");
            errorResp.put("mensaje", String.format("Stock insuficiente: solo quedan %.0f unidad(es) disponibles.", stockActual));
            errorResp.put("stockDisponible", stockActual);
            errorResp.put("cantidadSolicitada", cantidadPedida);
            errorResp.put("unidadMedida", insumo.getUnidadMedida());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResp);
        }

        // DESCONTAR STOCK AUTOMÁTICAMENTE
        double nuevoStock = stockActual - cantidadPedida;
        insumo.setStockActual(nuevoStock);
        insumoRepository.save(insumo);

        // REGISTRAR MOVIMIENTO DE SALIDA
        MovimientoInventario mov = new MovimientoInventario();
        mov.setTipo("salida");
        mov.setCantidad((double) cantidadPedida);
        mov.setFecha(LocalDateTime.now());
        mov.setInsumo(insumo);
        // Asignar primer usuario disponible o admin
        usuarioRepository.findAll().stream().findFirst().ifPresent(mov::setUsuario);
        movimientoRepository.save(mov);

        // GUARDAR PEDIDO
        double total = producto.getPrecio() * cantidadPedida;
        Pedido pedido = new Pedido();
        pedido.setProducto(producto);
        pedido.setCantidad(cantidadPedida);
        pedido.setPrecioUnitario(producto.getPrecio());
        pedido.setTotal(total);
        pedido.setCliente(cliente);
        pedido.setNotas(req.getNotas());
        pedido.setEstado("Pendiente");
        pedido.setFecha(LocalDateTime.now());
        Pedido savedPedido = pedidoRepository.save(pedido);

        // NOTIFICACIÓN AL ADMIN: NUEVO PEDIDO
        String tituloNotif = "🔔 ¡Nuevo Pedido #" + savedPedido.getIdPedido() + "!";
        String msgNotif = String.format(
            "Cliente: %s | %dx %s ($%.2f c/u) | Total: $%.2f",
            cliente, cantidadPedida, producto.getNombre(), producto.getPrecio(), total
        );
        notificacionRepository.save(new Notificacion(tituloNotif, msgNotif, "PEDIDO"));

        // VERIFICAR Y NOTIFICAR ALERTAS DE STOCK TRAS LA RESTA
        if (nuevoStock <= 0) {
            producto.setEstado("Agotado");
            notificacionRepository.save(new Notificacion(
                "❌ Producto Agotado",
                "El insumo '" + insumo.getNombre() + "' se ha agotado por completo tras el último pedido. El producto '" + producto.getNombre() + "' ahora está Agotado.",
                "STOCK_AGOTADO"
            ));
        } else if (nuevoStock <= insumo.getStockMinimo()) {
            notificacionRepository.save(new Notificacion(
                "⚠️ Alerta: Stock Bajo",
                "El insumo '" + insumo.getNombre() + "' ha alcanzado su stock mínimo. Quedan " + nuevoStock + " " + insumo.getUnidadMedida() + ".",
                "STOCK_BAJO"
            ));
        }
        productoRepository.save(producto);

        Map<String, Object> okResp = new HashMap<>();
        okResp.put("success", true);
        okResp.put("mensaje", "¡Pedido realizado con éxito!");
        okResp.put("pedido", savedPedido);
        okResp.put("nuevoStock", nuevoStock);
        return ResponseEntity.ok(okResp);
    }

    @GetMapping("/pedidos")
    public List<Pedido> getPedidos(@RequestParam(required = false) String estado) {
        if (estado != null && !estado.isBlank()) {
            return pedidoRepository.findByEstadoOrderByFechaDesc(estado);
        }
        return pedidoRepository.findTop50ByOrderByFechaDesc();
    }

    @PutMapping("/pedidos/{id}/estado")
    public ResponseEntity<?> updatePedidoEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String nuevoEstado = body.get("estado");
        return pedidoRepository.findById(id).map(pedido -> {
            pedido.setEstado(nuevoEstado);
            Pedido saved = pedidoRepository.save(pedido);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ==========================================
    // 5. NOTIFICACIONES
    // ==========================================

    @GetMapping("/notificaciones")
    public List<Notificacion> getNotificaciones() {
        return notificacionRepository.findTop20ByOrderByFechaDesc();
    }

    @GetMapping("/notificaciones/resumen")
    public Map<String, Object> getNotificacionesResumen() {
        Map<String, Object> res = new HashMap<>();
        res.put("totalNoLeidas", notificacionRepository.countByLeidaFalse());
        res.put("ultimas", notificacionRepository.findTop20ByOrderByFechaDesc());
        return res;
    }

    @PutMapping("/notificaciones/marcar-todas-leidas")
    public ResponseEntity<?> marcarTodasLeidas() {
        List<Notificacion> noLeidas = notificacionRepository.findByLeidaFalseOrderByFechaDesc();
        for (Notificacion n : noLeidas) {
            n.setLeida(true);
        }
        notificacionRepository.saveAll(noLeidas);
        return ResponseEntity.ok(Map.of("mensaje", "Todas las notificaciones marcadas como leídas"));
    }

    @PutMapping("/notificaciones/{id}/leer")
    public ResponseEntity<?> marcarLeida(@PathVariable Long id) {
        return notificacionRepository.findById(id).map(n -> {
            n.setLeida(true);
            notificacionRepository.save(n);
            return ResponseEntity.ok(n);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/notificaciones/{id}")
    public ResponseEntity<?> eliminarNotificacion(@PathVariable Long id) {
        notificacionRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Notificación eliminada"));
    }

    // ==========================================
    // 6. MOVIMIENTOS
    // ==========================================

    @GetMapping("/movimientos")
    public List<MovimientoInventario> getMovimientos() {
        return movimientoRepository.findAllByOrderByFechaDesc();
    }

    @PostMapping("/movimientos")
    public ResponseEntity<?> createMovimiento(@RequestBody MovimientoRequest req) {
        Insumo insumo = insumoRepository.findById(req.getIdInsumo()).orElse(null);
        Usuario usuario = usuarioRepository.findById(req.getIdUsuario()).orElse(null);

        if (insumo == null || usuario == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Insumo o usuario no válido"));
        }

        MovimientoInventario mov = new MovimientoInventario();
        mov.setTipo(req.getTipo());
        mov.setCantidad(req.getCantidad());
        mov.setFecha(LocalDateTime.now());
        mov.setInsumo(insumo);
        mov.setUsuario(usuario);

        // Actualizar stock
        if ("entrada".equalsIgnoreCase(req.getTipo())) {
            insumo.setStockActual(insumo.getStockActual() + req.getCantidad());
        } else if ("salida".equalsIgnoreCase(req.getTipo())) {
            double res = insumo.getStockActual() - req.getCantidad();
            insumo.setStockActual(Math.max(0, res));
        }

        insumoRepository.save(insumo);
        movimientoRepository.save(mov);

        // Actualizar estado de productos relacionados
        List<Producto> productos = productoRepository.findByInsumo_IdInsumo(insumo.getIdInsumo());
        for (Producto p : productos) {
            actualizarEstadoProducto(p);
        }

        return ResponseEntity.ok(mov);
    }

    // ==========================================
    // 7. DASHBOARD STATS
    // ==========================================

    @GetMapping("/dashboard/stats")
    public Map<String, Object> getStats() {
        List<Insumo> insumos = insumoRepository.findAll();
        List<Producto> productos = productoRepository.findAll();
        List<Pedido> pedidos = pedidoRepository.findAll();

        long insumosActivos = insumos.size();
        long stockBajo = insumos.stream().filter(i -> i.getStockActual() > 0 && i.getStockActual() <= i.getStockMinimo()).count();
        long agotados = insumos.stream().filter(i -> i.getStockActual() <= 0).count();
        long prodMenu = productos.size();
        long pedidosPendientes = pedidos.stream().filter(p -> "Pendiente".equalsIgnoreCase(p.getEstado())).count();
        long notificacionesNoLeidas = notificacionRepository.countByLeidaFalse();

        Map<String, Object> stats = new HashMap<>();
        stats.put("insumosActivos", insumosActivos);
        stats.put("stockBajo", stockBajo);
        stats.put("agotados", agotados);
        stats.put("productosMenu", prodMenu);
        stats.put("pedidosPendientes", pedidosPendientes);
        stats.put("totalPedidos", pedidos.size());
        stats.put("notificacionesNoLeidas", notificacionesNoLeidas);
        return stats;
    }

    private void actualizarEstadoProducto(Producto p) {
        if (p.getInsumo() != null) {
            if (p.getInsumo().getStockActual() == null || p.getInsumo().getStockActual() <= 0) {
                p.setEstado("Agotado");
            } else {
                p.setEstado("Disponible");
            }
            productoRepository.save(p);
        }
    }
}
