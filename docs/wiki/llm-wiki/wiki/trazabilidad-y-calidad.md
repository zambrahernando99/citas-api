# Trazabilidad y calidad

## EVIDENCIA — Oferta profesional S3 (2026-09-24)

- HU cubiertas: HU-011, HU-012 y HU-013; dependencias declaradas HU-004, HU-007 y HU-010.
- Backend: migración Flyway V2 con perfiles profesionales, catálogos de dos sedes fijas, especialidades activas y relaciones N:M normalizadas; autorización de las mutaciones por `ROLE_ADMIN`.
- Seguridad: la contraseña temporal sólo se acepta en el request de creación, se transforma con BCrypt y no figura en respuestas ni logs de aplicación.
- Frontend: las vistas de profesionales y especialidades consultan la API REST; ADMIN puede crear, asignar y activar/desactivar sin usar las listas simuladas de esas vistas.
- Pruebas ejecutadas: `mvn test` con repositorio Maven temporal del workspace — 15 pruebas, 0 fallos. Incluye rol, unicidad, BCrypt, relaciones N:M, primaria, sedes, catálogo y estado activo.
- Verificación frontend: `npm run lint` y `npm run build` — correctos. El build se ejecutó fuera del sandbox porque Vite requiere sus binarios nativos de Windows.
- Verificación MySQL real (2026-09-24): Docker Desktop inició `mysql:8.4` con `database/reference/db.sql`; la API aplicó Flyway V2 en `citas_fcv_training` y `GET /actuator/health` respondió 200/UP. Se confirmó el flujo REST ADMIN `201`/`200`, duplicado `409`, USER sin permiso `403`, desactivación y persistencia de 2 especialidades, exactamente 1 primaria, 2 sedes y hash BCrypt. No quedaron filas Flyway fallidas.
- Frontend real: Vite respondió 200 en `http://localhost:5173` y la API está disponible en `http://localhost:8080` durante esta sesión de validación.

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

## EVIDENCIA — Inicialización desde esquema de referencia (2026-09-22)

- Tras la autorización explícita del usuario, el volumen MySQL se recreó con `database/reference/db.sql` montado de solo lectura en el inicializador oficial de MySQL.
- Verificado en MySQL: 3 roles, 15 usuarios, 3 citas y 224 slots del esquema de referencia. Flyway registró baseline `0` y V1 correcta; el backend mantiene las tablas de autenticación S2 como compatibilidad transitoria.
- Health API devolvió `200`; un registro/login sintético sobre esa misma base devolvió `201` y tokens separados; `mvn test` completó 9 pruebas sin fallos ni errores.
