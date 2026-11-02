# ☕ CafeStock - Sistema Integral de Gestión de Cafetería e Inventario

Bienvenido a **CafeStock**, una aplicación web moderna y robusta construida con **Spring Boot**, **Spring Data JPA**, **MySQL**, **BCrypt** y **Bootstrap 5**.

El sistema permite la administración completa de productos, insumos, inventario, auditoría de movimientos y pedidos en tiempo real con control estricto de stock, alertas sonoras y visuales, y autenticación encriptada de alta seguridad.

---

## 🌟 Resumen de Nuevas Funcionalidades y Mejoras Implementadas

### 1. 🛡️ Encriptación y Seguridad de Contraseñas (BCrypt)
- **Migración Automática**: Al iniciar la aplicación, las contraseñas existentes (`admin123`, `barista123`) son detectadas y encriptadas automáticamente con **BCrypt** en la base de datos MySQL.
- **Nuevos Usuarios Protegidos**: Cualquier usuario creado en el sistema pasa por un hash unidireccional BCrypt (`$2a$10$...`).
- **Seguridad en Respuestas API**: Los endpoints devuelven objetos de usuario seguros sin exponer los hashes de contraseña en el frontend ni en las respuestas JSON.

### 2. ☕ CRUD Mejorado de Productos (Panel Admin)
- **Gestión Completa**: El administrador puede **ver**, **crear**, **editar** y **eliminar** productos fácilmente mediante modales interactivos.
- **Vinculación con Insumos**: Cada producto se asocia a un insumo específico del inventario (ej: Café en grano, Croissants, Leche, Jarabe de Caramelo).
- **Estado Dinámico**: El estado del producto (*Disponible* o *Agotado*) se calcula y actualiza automáticamente según la existencia real del insumo asociado.
- **Búsqueda y Filtros**: Buscador instantáneo por nombre y categoría.

### 3. 🛍️ Pantalla Interactiva de Pedidos (Lado del Cliente)
- **Modal de Pedido**: Al hacer clic en *"Pedir ahora"*, se despliega una pantalla interactiva con:
  - Nombre del producto y categoría.
  - Precio unitario.
  - Indicador de stock disponible en tiempo real.
  - Selector de cantidad con botones `+` y `-` (con límite de validación según el stock existente).
  - Nombre del cliente o número de mesa (opcional).
  - Instrucciones o notas especiales (ej: *"Sin azúcar"*, *"Leche de almendras"*).
  - Cálculo dinámico del **Total a Pagar** en tiempo real.
- **Ticket de Confirmación**: Modal de éxito que muestra el resumen de la orden generada (# de orden, producto, cantidad y total).

### 4. 📦 Descuento Automático de Stock y Auditoría
- Al confirmar el pedido, el backend descuenta automáticamente la cantidad requerida del insumo asociado.
- Se genera un registro formal en la tabla de **Movimientos de Inventario** de tipo `salida` para fines de trazabilidad y auditoría.
- Si el stock de un insumo llega a 0, el producto pasa inmediatamente a estado **Agotado** y se inhabilita para futuros pedidos.

### 5. ⚠️ Detección y Avisos de Stock Insuficiente
- Si un cliente intenta pedir una cantidad mayor a la disponible:
  - **Aviso al Cliente**: La pantalla del usuario muestra una alerta clara indicando que no hay suficiente stock y cuántas unidades quedan.
  - **Alerta al Administrador**: Se registra automáticamente una notificación de tipo `STOCK_INSUFICIENTE` en el panel de control indicando quién intentó hacer el pedido, qué producto y cuánto stock faltó.

### 6. 🔔 Centro de Notificaciones en Vivo para el Administrador
- **Campana con Contador**: Badge animado con efecto *pulse* que muestra el número de notificaciones pendientes.
- **Alerta Toast Flotante**: Ventana emergente en la esquina inferior derecha cada vez que entra un pedido o se dispara una alerta.
- **Chime Sonoro Sintetizado**: Reproducción de un timbre agradable mediante la *Web Audio API* nativa del navegador cuando hay eventos nuevos.
- **Panel Lateral (Offcanvas)**: Visualización del historial de notificaciones con opción de marcarlas como leídas.

### 7. 📋 Monitor y Despacho de Pedidos
- Pestaña dedicada en el panel del administrador para monitorear pedidos entrantes.
- Botón **"Despachar"** que cambia el estado de la orden de *"Pendiente"* a *"Completado"*.

---

## 🔑 Credenciales de Acceso Demostrativas

| Rol | Correo Electrónico | Contraseña | Encriptación |
| :--- | :--- | :--- | :--- |
| **Administrador** | `admin@cafestock.com` | `admin123` | BCrypt (`$2a$10$...`) |
| **Barista** | `juan@cafestock.com` | `barista123` | BCrypt (`$2a$10$...`) |

---

## 🚀 Guía Paso a Paso para Probar y Explicar el Proyecto

Sigue este guión durante tu presentación o prueba técnica para demostrar cada una de las funcionalidades implementadas:

### Paso 1: Comprobar la Encriptación en Base de Datos (BCrypt)
1. Abre tu gestor de base de datos MySQL (phpMyAdmin, MySQL Workbench o consola).
2. Ejecuta la consulta:
   ```sql
   SELECT id_usuario, nombre, correo, contrasena, rol FROM cafestock.usuario;
   ```
3. Observa que las contraseñas de `admin@cafestock.com` y `juan@cafestock.com` ya **no están en texto plano**, sino que inician con el prefijo seguro `$2a$10$...`.
4. *Punto a explicar*: "La aplicación integra `BCryptPasswordEncoder` de Spring Security Crypto. Al arrancar, el `DataInitializer` detecta contraseñas no encriptadas y las migra de forma transparente a hashes seguros".

---

### Paso 2: Iniciar Sesión en el Panel de Administración
1. Abre tu navegador y dirígete a:
   [http://localhost:8080/login.html](http://localhost:8080/login.html)
2. Ingresa con las credenciales:
   - Correo: `admin@cafestock.com`
   - Contraseña: `admin123`
3. Haz clic en **"Iniciar Sesión"**.
4. El sistema verificará el hash BCrypt y te redirigirá a [http://localhost:8080/admin.html](http://localhost:8080/admin.html), mostrando en la esquina superior *"Admin"* y *"Rol: admin"*.

---

### Paso 3: Probar el CRUD Mejorado de Productos (Admin)
1. En la pestaña **"Gestión de Productos (CRUD)"**:
   - Observa el catálogo con su ID, Nombre, Categoría, Precio, Insumo Vinculado, Stock Actual y Estado (*Disponible* / *Agotado*).
2. **Crear un Nuevo Producto**:
   - Haz clic en el botón azul **"+ Nuevo Producto"**.
   - Ingresa:
     - Nombre: `Capuccino Vainilla Francesa`
     - Categoría: `Bebidas Calientes`
     - Precio: `3.25`
     - Insumo Vinculado: `Café en grano` (o `Leche entera`)
   - Presiona **"Guardar Producto"**.
   - *Verificación*: El producto aparece instantáneamente en la tabla.
3. **Editar el Producto**:
   - En la fila del producto recién creado, haz clic en el botón azul de editar (icono de lápiz ✏️).
   - Cambia el precio a `3.50` y la categoría a `Especialidades`.
   - Guarda los cambios y comprueba que se actualizó.
4. **Buscador en Tiempo Real**:
   - En la barra de búsqueda de productos escribe `Capuccino`. Observa cómo filtra la tabla al instante.

---

### Paso 4: Realizar un Pedido desde la Pantalla de Usuario (Cálculo de Precios y Cantidades)
1. Abre una nueva pestaña o ventana en:
   [http://localhost:8080/index.html](http://localhost:8080/index.html) (Menú del Cliente).
2. Verás la tarjeta del producto recién creado junto a los demás productos con sus respectivos precios e insignias de stock.
3. Haz clic en el botón **"☕ Pedir ahora"** de cualquier producto disponible (ej: *Café Americano* o *Capuccino Vainilla Francesa*).
4. Se abrirá la **Pantalla de Pedido**:
   - Muestra el precio unitario y el stock disponible.
   - Presiona los botones `+` o `-` para cambiar la cantidad (ej: 2 unidades).
   - Observa cómo el **"Total a Pagar"** se multiplica automáticamente ($1.75 x 2 = $3.50).
   - Escribe en el campo de cliente: `Mesa 5 - Andrea`.
   - Escribe en notas especiales: `Con poca azúcar y bien caliente`.
5. Haz clic en **"Confirmar Pedido"**.
6. Aparecerá el modal de éxito con el ticket del pedido confirmando la orden.

---

### Paso 5: Verificar la Notificación y Despacho en el Panel de Admin
1. Regresa a la pestaña del Administrador ([http://localhost:8080/admin.html](http://localhost:8080/admin.html)).
2. Sin recargar la página, notarás:
   - Sonó un timbre (*chime* suave).
   - En la esquina inferior derecha apareció una ventana **Toast** anunciando el nuevo pedido.
   - La campana de notificaciones 🔔 tiene un número rojo pulsante.
   - El contador de **Pedidos Pendientes** subió.
3. Haz clic en la campana 🔔 para ver el detalle de la notificación: *"¡Nuevo Pedido! Cliente: Mesa 5 - Andrea | 2x ..."*.
4. Ve a la pestaña **"Pedidos en Vivo & Despacho"**:
   - Verás la orden con el cliente, notas, cantidad, total y estado *"Pendiente"*.
5. Haz clic en el botón verde **"Despachar"**:
   - La orden pasa a estado *"Completado"* y los contadores se actualizan.

---

### Paso 6: Comprobar el Descuento Automático de Stock
1. Ve a la pestaña **"Inventario de Insumos"** del Administrador:
   - Observa que el insumo asociado se redujo exactamente por la cantidad pedida.
2. Ve a la pestaña **"Historial de Movimientos"**:
   - Verás una fila de tipo `salida` registrada automáticamente por el sistema con la cantidad descontada y fecha exacta.

---

### Paso 7: Probar la Detección y Rechazo por Stock Insuficiente
1. Vuelve a la pestaña del Menú ([http://localhost:8080/index.html](http://localhost:8080/index.html)).
2. Elige un producto que tenga poco stock (por ejemplo, con 5 unidades disponibles).
3. Haz clic en **"Pedir ahora"** y en la cantidad ingresa un número mayor al stock (ej: `50`).
4. **Respuesta al Usuario**:
   - La pantalla de pedido mostrará inmediatamente una alerta roja:
     `❌ Stock insuficiente: solo quedan X unidades disponibles.`
   - El pedido **no se procesa** y no se descuenta inventario erróneamente.
5. **Notificación al Administrador**:
   - En el panel del Administrador, la campana se activa y se registra la alerta:
     `⚠️ Pedido Rechazado: Stock Insuficiente. El cliente intentó pedir 50 unidades, pero solo hay X disponibles.`

---

### Paso 8: Probar Producto Agotado Automático
1. Si un producto agota su stock (o si un insumo llega a 0 mediante pedidos o ajuste manual):
   - El estado del producto cambia automáticamente a **"Agotado"**.
   - En el menú del cliente, la tarjeta se atenúa en color gris y el botón se transforma en `Agotado` deshabilitado.
   - El Administrador recibe la notificación: `❌ Producto Agotado`.
2. Para reactivarlo:
   - El Administrador va a **"Inventario de Insumos"** -> **"Registrar Entrada / Salida"** (o en [http://localhost:8080/movimientos.html](http://localhost:8080/movimientos.html)).
   - Selecciona el insumo, marca **Entrada (Compra)**, ingresa una cantidad (ej: 10) y guarda.
   - Al instante el producto vuelve a estar **"Disponible"** en el Menú.

---

## 🛠️ Estructura del Código Fuente

```text
ADP-PROYECTO/
│
├── cafestock.sql                     # Script de inicialización de Base de Datos MySQL
├── README.md                         # Documentación completa y guía de prueba
└── cafestock/                        # Proyecto Spring Boot Maven
    ├── pom.xml                       # Configuración y dependencias (Spring Boot, Security Crypto, JPA, MySQL)
    └── src/
        ├── main/
        │   ├── java/com/dss/cafestock/
        │   │   ├── CafestockApplication.java     # Clase principal de ejecución
        │   │   ├── config/
        │   │   │   ├── AppConfig.java           # Configuración de Bean PasswordEncoder (BCrypt)
        │   │   │   └── DataInitializer.java     # Encriptación automática de usuarios iniciales
        │   │   ├── controller/
        │   │   │   ├── ApiController.java       # Controladores REST (Auth, Insumos, Productos, Pedidos, Notificaciones)
        │   │   │   ├── MovimientoRequest.java   # DTO para registro de movimientos
        │   │   │   └── PedidoRequest.java       # DTO para realización de pedidos
        │   │   ├── model/
        │   │   │   ├── Usuario.java             # Entidad Usuario
        │   │   │   ├── Producto.java            # Entidad Producto
        │   │   │   ├── Insumo.java              # Entidad Insumo
        │   │   │   ├── MovimientoInventario.java# Entidad Auditoría de Stock
        │   │   │   ├── Pedido.java              # Entidad Ordenes / Pedidos
        │   │   │   └── Notificacion.java        # Entidad Alertas y Notificaciones Admin
        │   │   └── repository/
        │   │       ├── UsuarioRepository.java
        │   │       ├── ProductoRepository.java
        │   │       ├── InsumoRepository.java
        │   │       ├── MovimientoInventarioRepository.java
        │   │       ├── PedidoRepository.java
        │   │       └── NotificacionRepository.java
        │   └── resources/
        │       ├── application.properties       # Conexión JDBC MySQL y configuración de Hibernate
        │       └── static/
        │           ├── index.html               # Menú público del cliente y pantalla modal de pedido
        │           ├── admin.html               # Panel Admin (CRUD productos, pedidos en vivo, notificaciones, insumos)
        │           ├── login.html               # Inicio de sesión con autenticación BCrypt
        │           └── movimientos.html         # Formulario de entrada / salida de inventario
        └── test/
```

---

## 📡 Referencia de Endpoints REST API

### Autenticación y Usuarios
- `POST /api/login` - Valida credenciales contra el hash BCrypt y retorna datos seguros.
- `GET /api/usuarios` - Lista usuarios sin exponer contraseñas.
- `POST /api/usuarios` - Registra un nuevo usuario encriptando la contraseña con BCrypt.

### Productos (CRUD Admin)
- `GET /api/productos` - Lista todos los productos con estado de stock actualizado.
- `GET /api/productos/{id}` - Obtiene un producto individual.
- `POST /api/productos` - Crea un nuevo producto y lo vincula a un insumo.
- `PUT /api/productos/{id}` - Modifica un producto existente.
- `DELETE /api/productos/{id}` - Elimina un producto.

### Pedidos
- `POST /api/pedidos` - Realiza un pedido, valida stock, descuenta inventario y notifica al admin.
- `GET /api/pedidos` - Lista las órdenes recibidas en orden cronológico inverso.
- `PUT /api/pedidos/{id}/estado` - Cambia el estado de la orden (ej: "Completado").

### Notificaciones
- `GET /api/notificaciones` - Retorna las notificaciones recientes.
- `GET /api/notificaciones/resumen` - Retorna el total de no leídas y las últimas 20.
- `PUT /api/notificaciones/marcar-todas-leidas` - Marca todas como leídas.

### Insumos y Movimientos
- `GET /api/insumos` - Lista el inventario de materias primas.
- `POST /api/insumos` - Registra un nuevo insumo.
- `PUT /api/insumos/{id}` - Actualiza un insumo existente.
- `DELETE /api/insumos/{id}` - Elimina un insumo (valida si no tiene productos asociados).
- `GET /api/movimientos` - Historial de entradas y salidas de inventario.
- `POST /api/movimientos` - Registro manual de movimiento (compra o merma).

---

## 💻 Instrucciones para Iniciar el Servidor

1. **Asegurar MySQL Activo**: Verifica que el servicio MySQL (o XAMPP/WAMP) esté ejecutándose en el puerto `3306` con la base de datos `cafestock`.
2. **Ejecutar el Proyecto**:
   Abre una terminal en la carpeta `cafestock` y ejecuta:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
   o ejecuta el archivo `.jar` compilado:
   ```powershell
   java -jar target/cafestock-0.0.1-SNAPSHOT.jar
   ```
3. **Acceder a la Aplicación**:
   - **Menú Cliente**: [http://localhost:8080/index.html](http://localhost:8080/index.html)
   - **Panel Administrador**: [http://localhost:8080/admin.html](http://localhost:8080/admin.html)
   - **Inicio de Sesión**: [http://localhost:8080/login.html](http://localhost:8080/login.html)
