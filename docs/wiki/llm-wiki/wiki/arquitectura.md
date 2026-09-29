# Arquitectura

## HECHOS

- `citas-api` usa Java 21, Spring Boot 3.5.x, Maven, arquitectura hexagonal, Spring Data JPA, MySQL 8.4, Flyway, Spring Security y JWT access/refresh. Fuente: restricciones técnicas.
- `citas-web` será React o Angular con TypeScript y Node.js 24 LTS, decidido después del flujo Stitch/Google AI Studio. Fuente: README y restricciones técnicas.
- No hay Express ni BFF; el frontend consume REST/JSON de Spring Boot y configura la URL backend por environment. Fuente: PRD RF-20 y restricciones técnicas.
- La raíz orquesta dos repositorios Git independientes. `main` es estable y `develop` es la rama de trabajo. Fuente: README y restricciones técnicas.

## PREFERENCIA

- Mantener contratos, decisiones y síntesis globales en esta wiki única; no duplicarlos sin enlace desde el repositorio web.
