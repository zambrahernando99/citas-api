# Decisiones

## DEC-001 — Ubicación y gobierno de la wiki

- Estado: aprobada.
- Fecha: 2026-09-17.
- Decisión: la única LLM Wiki global reside en `citas-api/docs/wiki/llm-wiki/`, con RAW, WIKI, SCHEMA, índice y log.
- Consecuencia: los cambios cross-repo se documentan aquí; la wiki no contiene secretos ni transcripciones.
- Evidencia de aprobación: aprobación explícita del usuario al plan inicial.

## DEC-002 — Referencia de base de datos diferida

- Estado: aprobada.
- Fecha: 2026-09-17.
- Decisión: excluir `database/reference/` del INGEST inicial.
- Consecuencia: el diseño del estudiante parte del PRD y requisitos 3FN; la comparación ocurre cuando el trainer la autorice.
- Evidencia de aprobación: instrucciones del workspace y corte inicial aprobado.

## DEC-003 — Sesiones JWT de HU-002

- Estado: aprobada.
- Fecha: 2026-09-17.
- Decisión: access JWT de 15 minutos y refresh JWT de 7 días, ambos configurados exclusivamente por variables de entorno; el refresh se rota y revoca por sesión.
- Consecuencia: se persiste por sesión únicamente un identificador no secreto, su hash de token, revocación y expiración. No se persiste el refresh crudo. La renovación revoca atómicamente el token presentado y emite un par nuevo; logout revoca sólo esa sesión.
- Evidencia de aprobación: instrucción explícita del usuario al aprobar HU-001/HU-002 y solicitar su implementación.

## DEC-004 — Aprobación del backlog de historias de usuario

- Estado: aprobada.
- Fecha: 2026-09-22.
- Decisión: aprobar las HU-001 a HU-028 para su planificación e implementación.
- Consecuencia: cada HU puede pasar a implementación cuando se seleccione para un incremento; la aprobación no altera su DoD ni equivale a implementación, validación o completitud.
- Evidencia de aprobación: instrucción explícita del usuario: “Puedes aprobar todas las HU”.

## DEC-005 — Esquema de referencia como inicialización de MySQL

- Estado: aprobada.
- Fecha: 2026-09-22.
- Decisión: por autorización explícita del usuario, MySQL inicializa el esquema con `database/reference/db.sql`, montado de solo lectura en `docker-entrypoint-initdb.d`.
- Consecuencia: las tablas de catálogo, disponibilidad, citas y datos sintéticos de la referencia quedan disponibles desde la creación del volumen. Flyway conserva temporalmente las tablas de identidad de S2 (`user_account`, `role_catalog`, `auth_session`) para no romper el contrato REST existente; su convergencia con `users`, `roles` y `refresh_tokens` se aborda en una HU posterior y no se declara completada aquí.
- Evidencia de aprobación: confirmación explícita del usuario para usar `C:\Users\IA ACADEMY 3\Documents\hernando\citas\citas\database\reference\db.sql`.
- Enmienda 2026-10-01 (autorizada por el usuario): HECHO, `db.sql` crea y usa `citas_fcv_training`; si la BD de la app tiene ese nombre, Flyway falla en V3. DECISIÓN: la app usa `MYSQL_DATABASE`/`DB_NAME=citas_app` (valor por defecto en `docker-compose.yml` y `.env.example`); `db.sql` sigue montado y deja la referencia en `citas_fcv_training` solo para consulta. Evidencia: un MySQL 8.4 nuevo con el montaje creó ambas BD (21 tablas de referencia) y la API aplicó V1..V7 en `citas_app` con health `UP`.

## DEC-006 — Desactivación operativa de profesional en S3

- Estado: aprobada.
- Fecha: 2026-09-24.
- Decisión: la desactivación conserva el perfil y todas sus relaciones de especialidades y sedes. El profesional inactivo queda excluido de la consulta de profesionales habilitados para operaciones nuevas. S3 no crea, modifica ni cancela compromisos, bloques, reservas o agenda: esas capacidades pertenecen a S4.
- Consecuencia: el estado se cambia mediante una actualización reversible; no existe borrado físico de profesional ni de sus asignaciones.
- Evidencia de aprobación: precondición confirmada por el usuario al solicitar la implementación de S3.

## DEC-007 — Recuperación, perfil y regímenes para S4

- Estado: aprobada por el usuario.
- Fecha: 2026-09-24.
- Decisiones: las retenciones de citas/reprogramaciones no vencen automáticamente y se liberan al decidir; el profesional sólo puede cerrar la atención después de la hora fin; el token de recuperación sólo se expone en perfil `dev`; el catálogo de regímenes será configurable por ADMIN; el perfil editable se limita a nombres, apellidos, email y teléfono.
- Consecuencias: el API conserva retenciones pendientes hasta resolución; la API/UI no expone tokens en ambientes no-dev; se implementa CRUD lógico de regímenes y el contrato anota la excepción autorizada a HU-007; `/auth/me` no devuelve documentos ni permite modificar roles/identidad.
- Evidencia de aprobación: respuestas explícitas del usuario a las preguntas de decisión durante esta implementación.

## DEC-008 — Cancelación mientras hay reprogramación pendiente

- Estado: aprobada por el usuario.
- Fecha: 2026-09-29.
- Decisión: bloquear la cancelación de la cita original mientras su solicitud de reprogramación siga `PENDING`; primero debe resolver ADMIN.
- Consecuencia: ni la franja original ni la nueva se liberan por inferencia; la UI indica que la cita está a la espera de decisión.
- Evidencia de aprobación: respuesta explícita del usuario a la pregunta de aclaración del 2026-09-29.

## DEC-009 — Regímenes fijos (2026-09-29)

- Estado: aprobada por el usuario.
- Decisión: el usuario eligió cumplir PRD RF-05; los regímenes son catálogo fijo de solo lectura. Sustituye la excepción de DEC-007 sobre regímenes configurables.
- Consecuencias: migración `V7__seed_fixed_regimes.sql` idempotente; se retiró el CRUD admin de regímenes en API y UI ("Regímenes (catálogo fijo)").

## DEC-010 — Hook de secretos en PowerShell nativo (2026-09-29)

- Estado: aprobada.
- Decisión: el hook pre-commit de detección de secretos se implementa en PowerShell nativo y analiza el contenido staged (no el archivo del árbol de trabajo).
- Evidencia: `docs/evidence/s3-s4/02-hook.md`.

## DEC-011 — Docker: volumen `web_node_modules` y perfil `dev` en API (2026-09-29)

- Estado: aprobada.
- Decisión: el contenedor web usa un volumen nombrado `web_node_modules` para aislar dependencias nativas del host Windows; el contenedor API arranca con el perfil Spring `dev`.
- Evidencia: `docs/evidence/s3-s4/03-e2e-docker.md`.

## DEC-012 — Vitest como runner de pruebas web (2026-09-29)

- Estado: aprobada por el usuario.
- Decisión: las pruebas de `citas-web` usan Vitest (`src/components/screens/*.test.tsx`).

## DEC-013 — Identidad de máquina para n8n (S5, 2026-10-01)

- Estado: aprobada (propuesta en `planS5.md`; el usuario no pidió alternativa y autorizó iniciar S5).
- Decisión: n8n se autentica con `X-Automation-Key` (variable `AUTOMATION_API_KEY`, nunca versionada) que solo concede `ROLE_AUTOMATION` sobre `/api/v1/automation/**`. Sin clave configurada los endpoints quedan cerrados.
- Alternativa descartada: cuenta de servicio con JWT (TTL de 15 min, contraseña que gestionar y acceso a rutas de usuario).
- Consecuencia: la clave se guarda en n8n como credencial *Header Auth*; el túnel ngrok además niega en el borde cualquier ruta fuera de `/api/v1/automation/` (`automations/ngrok/traffic-policy.yml`).

## DEC-014 — Idempotencia de recordatorios (S5, 2026-10-01)

- Estado: aprobada.
- Decisión: tabla `appointment_reminder_delivery` (V8) con `UNIQUE (appointment_id, window_code)`. `SENT` es definitivo; `FAILED` se reintenta en ejecuciones posteriores hasta 3 intentos. No se guarda email ni cuerpo del mensaje.

## DEC-015 — Destinatario en modo laboratorio (S5, 2026-10-01)

- Estado: aprobada.
- Decisión: los pacientes son sintéticos; el nodo `Config` de WF-001 tiene `testRecipient`. Si tiene valor, todos los correos van a ese buzón de laboratorio; el valor se configura en n8n y el JSON versionado lo deja vacío.

## DEC-016 — Datos mínimos hacia n8n (S5, 2026-10-01)

- Estado: aprobada.
- Decisión: el endpoint de recordatorios solo devuelve lo que el correo usa (ver contrato). Documento, teléfono, afiliación y motivos no salen del API.

## DEC-017 — Túnel ngrok para n8n remoto (S5, 2026-10-01)

- Estado: aprobada por el usuario ("ngrok").
- Decisión: la instancia n8n del trainer es remota y llega a la API local por `ngrok http 8080 --traffic-policy-file automations/ngrok/traffic-policy.yml`. El authtoken de ngrok es personal y vive solo en la configuración local de ngrok.

## DEC-018 — Outbox transaccional para WF-002 (S6, 2026-10-01)

- Estado: aprobada (el usuario pidió trabajar WF-002 y WF-003).
- Decisión: la transición y el evento se guardan en la misma transacción (`automation_event_outbox`, V9); un despachador programado los envía al webhook con reintentos. Si la transición falla, no hay evento; si n8n está caído, el evento espera.
- Alternativa descartada: llamar al webhook dentro de la petición del usuario (acopla la latencia y la disponibilidad de n8n a la API y pierde eventos si falla).

## DEC-019 — WF-003 agrupa en n8n con datos sin PII (S6, 2026-10-01)

- Estado: aprobada.
- Decisión: la API entrega las citas del día solo con sede, especialidad, estado y hora; n8n agrupa (nodo Code) y arma el correo. Si la API falla tras 3 intentos, se envía un correo de incidencia.

## DEC-020 — Instancia n8n compartida: sin credenciales ajenas (S6, 2026-10-01)

- Estado: aprobada.
- HECHO: la cuenta MCP opera en el proyecto personal del trainer, compartido con otros estudiantes. Al crear un workflow con nodo Gmail, n8n autoasignó la credencial OAuth de otro estudiante.
- Decisión: los workflows de Hernando llevan el prefijo `Hernando-`, se crean inactivos y sin credenciales; el nodo Gmail queda en `serviceAccount` (sin credenciales en la instancia) para evitar la autoasignación, y Hernando cambia a OAuth2 con su propia credencial. No se modifican workflows ni credenciales de otros.
