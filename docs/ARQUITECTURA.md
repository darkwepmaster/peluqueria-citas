# Arquitectura

## Vista general (MVC en capas + DAO)
```
Navegador ─HTTP→ [VISTA] Razor (frontend-csharp) ─ [CONTROLADOR MVC] ApiClient ─HTTP/JSON+JWT→
   [CONTROLADOR REST] Spring → [SERVICIO] reglas de negocio → [DAO/REPOSITORIO] → [MODELO] entidades JPA → BD
                                    └→ Exportadores (Strategy)         └→ Notificaciones → Worker C# → SMTP / WhatsApp
```

## MVC
- **Modelo:** entidades JPA (`model/`), DTOs (`dto/Dtos.java`) y reglas (`service/`).
- **Vista:** vistas Razor (`frontend-csharp/Views`).
- **Controlador:** controladores MVC de C# (flujo de pantallas) y controladores REST de Spring (`controller/`).

## DAO
`dao/CitaDao` (interfaz) + `dao/CitaDaoJpa` (EntityManager, JPQL parametrizado). Los servicios dependen de la interfaz. Para el resto de entidades se usan repositorios Spring Data (`repository/`).

## SOLID
| Principio | Evidencia |
|---|---|
| S — Responsabilidad única | `AuthService`, `CitaService`, `NotificacionService`, `ReporteService`, `ExportService` hacen una sola cosa; `Disponibilidad` y `Sanitizador` son utilidades puras. |
| O — Abierto/cerrado | `Exportador` (Csv, Excel): para un formato nuevo se crea otra clase. En C#, `IMensajero` (Correo, WhatsApp). |
| L — Sustitución de Liskov | Cualquier `Exportador`/`IMensajero` es intercambiable; `Cliente`, `Estilista`, `Administrador` sustituyen a `Usuario`. |
| I — Segregación de interfaces | `CitaDao`, `Exportador` e `IMensajero` son pequeñas y específicas. |
| D — Inversión de dependencias | Los servicios reciben `CitaDao` por constructor; en las pruebas se simula con Mockito. |

## TDD
Pruebas en `backend-java/src/test` (Disponibilidad, Sanitizador, Exportador, AuthService, CitaService con DAO simulado, mensajes) y `tests-csharp`. Ciclo: escribir la prueba de la regla → ver que falla → implementar → refactorizar.

## Seguridad
BCrypt · JWT firmado (clave por variable de entorno) · roles por ruta · bloqueo tras 5 intentos (Guava Cache) · validación Bean Validation · JPQL parametrizado · bloqueo pesimista contra doble reserva · neutralización de fórmulas en CSV/Excel · registros de auditoría sin datos sensibles · cookie HttpOnly y antifalsificación (`ValidateAntiForgeryToken`) en la web C#.

## Librerías de apoyo
| Librería | Uso |
|---|---|
| Google Guava | `Range` (cruce de horarios), `Cache` (intentos de login), `Multiset` (reportes), `ImmutableList`, `Maps.uniqueIndex` |
| Apache POI | `ExportadorExcel` (.xlsx) |
| Apache Commons | `commons-lang3` (`Sanitizador`), `commons-csv` (`ExportadorCsv`) |
| Logback | `logback-spring.xml`: consola, archivo con rotación y bitácora `AUDITORIA` |
