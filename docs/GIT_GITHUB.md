# Control de versiones con Git y GitHub

**Responsable de control de versiones (propuesto):** el líder del proyecto; el equipo puede reasignarlo.

## Estrategia de ramas (GitFlow simplificado)
- `main`: versión estable y entregable (se etiqueta: `v0.3.0`).
- `develop`: integración continua del equipo.
- `feature/<tema>`: una rama por funcionalidad; se integra a `develop` con *pull request* revisado por otro integrante.
- `docs/<tema>`, `fix/<tema>`: documentación y correcciones.

## Convención de commits
`feat:` funcionalidad · `fix:` corrección · `docs:` documentación · `test:` pruebas · `refactor:` · `chore:`.

## Subir el repositorio a GitHub
```bash
git remote add origin https://github.com/<usuario>/peluqueria-citas.git
git push -u origin main develop --tags
```
Luego cada integrante clona el repositorio, trabaja en su rama y abre *pull requests*. La integración continua (`.github/workflows/ci.yml`) compila y prueba Java y C# en cada *push*.

## Buenas prácticas
No subir contraseñas ni claves (usar variables de entorno) · commits pequeños y descriptivos · revisar los *pull requests* · no reescribir historia compartida.
