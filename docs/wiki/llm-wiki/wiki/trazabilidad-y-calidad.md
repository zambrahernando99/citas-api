# Trazabilidad y calidad

## HECHOS

- Se requiere al menos un commit trazable por sesión S2–S6 en cada repositorio, sin reescritura que oculte el progreso. Fuente: restricciones técnicas y evidencias.
- S3+ exige pruebas de dominio, aplicación e integración relevante para backend; build/typecheck y pruebas aplicables en frontend; y validación cross-repo de funcionalidades clave. Fuente: restricciones técnicas.
- La evidencia por sesión debe enlazar HU, criterios, pruebas, pendientes y commits. Fuente: `EVIDENCIAS_Y_TRAZABILIDAD.md`.

## PREFERENCIA

- Usar la HU aprobada como unidad primaria de alcance y DoD, vinculada a cambios, pruebas y contratos.

## EVIDENCIA — HU-001 y HU-002 (2026-09-17)

- `mvn test` completó con 9 pruebas, 0 fallos y 0 errores: una prueba unitaria de JWT, dos pruebas unitarias/de aplicación de registro y seis pruebas REST/persistencia.
- La migración Flyway `V1__create_identity_and_sessions.sql` y la integración H2 en modo MySQL fueron ejecutadas durante las pruebas; el runtime productivo queda configurado para MySQL 8.4 mediante variables de entorno.
- Se verificaron: registro USER, unicidad de email/documento, BCrypt sin exposición de password, credenciales inválidas, access/refresh separados, roles en access, refresh inválido/expirado/revocado/reutilizado, rotación, logout por sesión, CORS y autorización base.

## EVIDENCIA — GOAL 01 y validación cross-repo S2 (2026-09-22)

- `docker compose ps` confirmó MySQL 8.4 saludable y el contenedor Java 21 disponible. La API arrancó contra MySQL, aplicó/validó Flyway y respondió `200 {"status":"UP"}` en Actuator.
- `docker compose exec -T citas-api-dev mvn test` ejecutó 9 pruebas: 6 REST/persistencia, 1 JWT y 2 de registro; todas terminaron sin fallos ni errores.
- Flujo REST controlado, con datos exclusivamente sintéticos: preflight CORS para `http://localhost:5173`; registro `201`; login con access/refresh; rotación de refresh; logout `204`. No se registraron ni versionaron tokens o contraseñas.
- En `citas-web`, `npm run lint` y `npm run build` finalizaron correctamente. El formulario de registro/login usa el contrato REST v1 y muestra errores observables del servidor.
