# Botica Matifarma

Sistema web para llevar el inventario, las ventas y los vencimientos de la botica. Está hecho con Java 17, Spring Boot 3.3.5, Maven, Thymeleaf, Spring Security y SQL Server (acceso a datos con JDBC y patrón DAO). La interfaz usa Bootstrap 5.3.3.

## Qué se necesita

- JDK 17 o superior
- Maven 3.9 o superior
- SQL Server 2019 (o Express) con la autenticación SQL activada
- Conexión a internet al ejecutar, porque Bootstrap, los iconos y la tipografía se cargan desde un CDN

## Base de datos

1. Abrir `database/01_matifarma.sql` en SSMS o Azure Data Studio y ejecutarlo completo. Crea la base `matifarma`, las tablas, las restricciones y las categorías iniciales.
2. Si se quieren datos de prueba, ejecutar también `database/02_datos_demo.sql`. Carga 2 proveedores y 8 productos, algunos con stock bajo o a punto de vencer, para ver cómo funcionan las alertas.

## Configuración

Los valores se pueden cambiar en `src/main/resources/application.properties` o con variables de entorno:

| Variable | Valor por defecto |
|---|---|
| `DB_HOST` | `localhost` |
| `DB_PORT` | `1433` |
| `DB_NAME` | `matifarma` |
| `DB_USER` | `sa` |
| `DB_PASSWORD` | no tiene, hay que definirla |
| `DB_TRUST_CERT` | `true` (sirve para el certificado autofirmado de un SQL Server local; en producción usar `false` con un certificado válido) |
| `ADMIN_PASSWORD` | opcional; si no se define, se genera una al azar |
| `THYMELEAF_CACHE` | `true` (ponerla en `false` solo mientras se editan las plantillas) |

En Linux o macOS:

```bash
DB_PASSWORD='mi-clave' mvn spring-boot:run
```

En PowerShell:

```powershell
$env:DB_PASSWORD = "mi-clave"
mvn spring-boot:run
```

## Compilar y ejecutar

```bash
mvn clean install
mvn spring-boot:run
```

La aplicación queda en http://localhost:8080.

La primera vez que arranca, si la tabla `empleado` está vacía, se crea el usuario `admin`. Si se definió `ADMIN_PASSWORD`, esa es su clave. Si no, el sistema genera una temporal y la escribe en el log de arranque una sola vez, así que hay que copiarla de ahí. Conviene cambiarla enseguida desde el módulo Usuarios.

## Roles

| Rol | Qué ve en el menú |
|---|---|
| Administrador | Todo |
| Vendedor | Dashboard, Ventas y Consultar stock |
| Almacenero | Dashboard, Inventario y Consultar stock |

Los permisos se controlan en el servidor (`config/SecurityConfig.java`), no solo escondiendo opciones del menú. El precio de compra únicamente lo ve el administrador.

## Estructura del proyecto

```
src/main/java/com/matifarma/
  controller/   PaginaController (vistas) y los *ApiController (JSON)
  service/      reglas de negocio y validaciones del servidor
  dao/          consultas a SQL Server con JdbcTemplate
  model/        records con los datos y sus validaciones (Bean Validation)
  config/       seguridad, login, sesión y creación del administrador
  exception/    errores de negocio y respuesta JSON de error
src/main/resources/
  templates/    una página HTML por módulo, más fragments/layout.html
  static/css/   global.css y un CSS por página
  static/js/    un JS por página, más los compartidos
database/       scripts de SQL Server
```

Además de lo que pedía la estructura original, se agregaron `templates/fragments/layout.html` (sidebar, barra superior y cabecera compartidos), `static/img/logo.svg` y estos scripts compartidos en `static/js/`: `api.js`, `buscador-productos.js`, `notificaciones.js`, `modal.js` y `sidebar.js`.

## Cosas que conviene saber

- Doble envío: al enviar un formulario el botón se deshabilita, y cada ingreso, salida o venta lleva un token único que la base de datos no deja repetir. Si llega el mismo envío dos veces, el segundo se rechaza con un error 409.
- Stock: nunca queda en negativo. El descuento es una sola sentencia `UPDATE ... WHERE cantidad >= ?`, y además la tabla tiene un `CHECK (cantidad >= 0)`.
- Ventas: el servidor no confía en los precios que manda el navegador. Los toma de la base y recalcula subtotal y total. La venta, su detalle, el descuento de stock y el movimiento se guardan en una sola transacción.
- Ingresos con producto vencido: esas unidades no se suman al stock y el ingreso queda marcado como no conforme.
- Sesiones: en cada petición se comprueba que el empleado siga activo y con el mismo rol (`EstadoSesionFilter`). Si un administrador desactiva a alguien o le cambia el rol, el cambio se aplica en el acto.
- Contraseñas: se guardan con BCrypt (costo 12). Tras 5 intentos fallidos la cuenta se bloquea 15 minutos; ambos valores se ajustan en `matifarma.seguridad.*`.
- CSRF está activo en los formularios y en las llamadas `fetch`.
- Alertas: se generan por stock igual o menor al mínimo, por productos vencidos que aún tienen stock y por productos que vencen dentro de los próximos 30 días (`matifarma.alertas.dias-vencimiento`).