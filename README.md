# citas-api

Backend REST del proyecto de citas.

## Stack y ejecución
- Java 21 + Spring Boot 3.5.x + Maven.
- Arquitectura hexagonal.
- MySQL + Flyway.
- Spring Security + JWT access/refresh.
- REST directo consumido por `citas-web` (sin BFF).
- Las pruebas relevantes se mantienen en `src/test`; este incremento todavía requiere ejecutar su suite y validar las migraciones contra MySQL.

Configura las variables según `.env.example` (sin versionar secretos) y ejecuta `mvn spring-boot:run`. Los cambios de esquema se aplican con Flyway.

## Capacidades

Incluye identidad/sesiones, oferta profesional, disponibilidad y reservas; ciclo de vida de citas; perfil, recuperación de contraseña, afiliación EPS/plan/régimen y catálogos administrativos. El contrato REST cross-repo y las decisiones pendientes están en `docs/wiki/llm-wiki/`.

## Documentación compartida
- `docs/wiki/scrum/`: épicas/HU generadas con la Skill Scrum.
- `docs/wiki/llm-wiki/`: única LLM Wiki global del workspace.
- `automations/n8n/`: JSON exportados en S5/S6.

Los workflows n8n se exportan como JSON en S5/S6 sin credenciales embebidas y sólo se activan tras una prueba controlada.
