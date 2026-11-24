# Botica Matifarma — Sistema de gestión de inventario

Java 17 · Spring Boot 3.3.5 · Maven · Thymeleaf · Spring Security · JDBC (patrón DAO) · SQL Server · Bootstrap 5.3.3

## 1. Requisitos
- JDK 17 o superior
- Apache Maven 3.9+
- SQL Server 2019+ (o Express) con autenticación SQL habilitada
- Conexión a internet al ejecutar (Bootstrap, Bootstrap Icons y la tipografía se cargan por CDN)

## 2. Base de datos
1. Abra `database/01_matifarma.sql` en SSMS / Azure Data Studio y ejecútelo completo (crea la BD `matifarma`, tablas, restricciones y categorías).
2. *(Opcional)* Ejecute `database/02_datos_demo.sql` para cargar 2 proveedores y 8 productos de ejemplo (con stock bajo y próximos a vencer para ver las alertas).

## 3. Conexión
Edite `src/main/resources/application.properties` o defina variables de entorno:

| Variable | Valor por defecto |
|---|---|
| `DB_HOST` | `localhost` |
| `DB_PORT` | `1433` |
| `DB_NAME` | `matifarma` |
| `DB_USER` | `sa` |
| `DB_PASSWORD` | **obligatoria** (sin valor por defecto) |
| `DB_TRUST_CERT` | `true` (certificado autofirmado local; en producción use `false` con un certificado válido) |
| `ADMIN_PASSWORD` | opcional; si no se define se genera una aleatoria |
| `THYMELEAF_CACHE` | `true` (use `false` solo mientras edita plantillas) |

Ejemplo: `DB_PASSWORD='su-clave' mvn spring-boot:run`

## 4. Compilar y ejecutar
```bash
mvn clean install
mvn spring-boot:run
```
Abra http://localhost:8080

**Primer ingreso:** la primera vez que arranca (tabla `empleado` vacía) se crea el administrador
`admin`. Si definió `ADMIN_PASSWORD` se usa esa clave; si no, se genera una contraseña temporal
y se muestra **una sola vez en el log** de arranque. Cámbiela desde **Usuarios**.

## 5. Roles y menú
| Rol | Opciones visibles |
|---|---|
| Administrador | Todas |
| Vendedor | Dashboard, Ventas, Consultar stock |
| Almacenero | Dashboard, Inventario, Consultar stock |

Los permisos se aplican en el servidor (`config/SecurityConfig.java`), no solo ocultando el menú. El precio de compra solo lo ve el Administrador.

## 6. Estructura
```
src/main/java/com/matifarma/
  controller/  PaginaController (vistas) + *ApiController (JSON)
  service/     reglas de negocio y validaciones del servidor
  dao/         acceso a SQL Server con JdbcTemplate
  model/       records: entidades, DTO y validaciones (Bean Validation)
  config/      seguridad, login, sesión, administrador inicial
  exception/   errores de negocio y respuesta JSON uniforme
src/main/resources/
  templates/   una página HTML por módulo + fragments/layout.html (sidebar, topbar, head)
  static/css/  global.css + un CSS por página
  static/js/   un JS por página + api, validaciones, notificaciones, modal, sidebar, buscador-productos
database/      scripts SQL Server
```
Archivos que se agregaron a la estructura pedida: `templates/fragments/layout.html` (fragmentos compartidos), `static/js/api.js`, `buscador-productos.js`, `notificaciones.js`, `modal.js`, `sidebar.js`, `static/img/logo.svg`.

## 7. Cómo se cumplen las validaciones clave
- **Doble envío:** el botón se deshabilita al enviar y cada ingreso/salida/venta lleva un token único (columna `UNIQUE` en BD). Un reenvío se rechaza con 409.
- **Stock nunca negativo:** el descuento es una sola sentencia atómica (`UPDATE ... WHERE cantidad >= ?`) más un `CHECK (cantidad >= 0)`.
- **Ventas:** el servidor ignora los precios del navegador y recalcula subtotal y total; venta + detalle + stock + movimiento van en una transacción.
- **Ingreso con producto vencido:** no suma al stock y queda registrado como *no conforme* (RF09).
- **Sesiones:** en cada petición se revalida que el empleado siga activo y con el mismo rol (`EstadoSesionFilter`); desactivarlo o cambiarle el rol surte efecto de inmediato.
- **Contraseñas:** BCrypt (costo 12). Bloqueo de 15 min tras 5 intentos fallidos (`matifarma.seguridad.*`).
- **CSRF** activo en todos los formularios y llamadas `fetch`.
- **Alertas:** stock ≤ mínimo, vencidos con stock y por vencer en 30 días (`matifarma.alertas.dias-vencimiento`).
