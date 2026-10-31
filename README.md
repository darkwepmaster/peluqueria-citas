# Bella Estilo — Sistema de Gestión y Reserva de Citas para una Peluquería

Proyecto del curso **Integrador I: Sistemas – Software (UTP)** · Avances de Proyecto Final 2 y 3.

Aplicación de **dos partes** que se comunican por HTTP/JSON:

| Carpeta | Tecnología | Rol |
|---|---|---|
| `backend-java/` | **Java 17 · Spring Boot 3** · JPA · Spring Security (JWT) · Guava · Apache POI · Apache Commons · Logback | API REST: reglas de negocio, seguridad, datos, reportes y exportación |
| `frontend-csharp/` | **C# · ASP.NET Core 8 MVC (Razor)** | Interfaz web (cliente, recepción y estilista) que consume la API |
| `recordatorios-csharp/` | **C# · .NET 8 Worker Service** | Despacha por correo/WhatsApp las notificaciones vencidas |
| `tests-csharp/` | xUnit | Pruebas del cliente de la API y de los formatos |
| `db/` | SQL | Diseño físico de referencia (MySQL 8) |
| `docs/` | Markdown | Arquitectura y guía de Git/GitHub |

> Proporción de código (líneas no vacías de `.java`, `.cs` y `.cshtml`): ver `docs/ARQUITECTURA.md`. **Java es la mayor parte (≥ 50 %)**, como exige el curso.

## Requisitos
JDK 17+, Maven 3.9+, .NET SDK 8. (MySQL 8 es opcional: por defecto la API usa H2 en memoria.)

## Ejecución rápida (3 terminales)
```bash
# 1) API Java  → http://localhost:8080
cd backend-java && mvn spring-boot:run

# 2) Interfaz web C#  → http://localhost:5000
cd frontend-csharp && dotnet run --urls http://localhost:5000

# 3) (Opcional) Despachador de recordatorios
cd recordatorios-csharp && dotnet run
```
Con MySQL: `mvn spring-boot:run -Dspring-boot.run.profiles=mysql` (variables `DB_USER`, `DB_PASSWORD`). La clave JWT se define con `JWT_SECRET` (mínimo 32 caracteres).

## Usuarios de demostración (solo pruebas — cambiar o poner `app.seed=false` en producción)
| Rol | Usuario | Contraseña |
|---|---|---|
| Administrador / recepción | admin@bellaestilo.pe | Admin123! |
| Cliente | valeria@correo.com | Cliente123! |
| Estilista | milagros@bellaestilo.pe | Estilista123! |

## Pruebas y documentación
```bash
cd backend-java && mvn test              # pruebas JUnit 5 + Mockito
cd backend-java && mvn javadoc:javadoc   # Javadoc en target/site/apidocs/index.html
dotnet test tests-csharp/PeluqueriaWeb.Tests
```

## Funcionalidades
Registro e inicio de sesión (JWT, roles) · catálogo · reserva en 4 pasos sin cruces · mis citas (cancelar/reprogramar) · notificaciones · panel de recepción · gestión de citas, servicios, estilistas y horarios · clientes · reportes R1–R3 con exportación a Excel/CSV · agenda del estilista.

Pendiente: restablecer contraseña por correo (RF03) y pantalla para cambiar canales de aviso (RF17; hoy se eligen al registrarse).

## Estado de verificación
El código fue escrito sin acceso a Maven Central ni a NuGet, por lo que **no se compiló ni se ejecutó en el entorno de generación**. Antes de entregar: ejecutar `mvn test` y `dotnet build`, corregir lo que aparezca y tomar capturas de pantalla reales.
