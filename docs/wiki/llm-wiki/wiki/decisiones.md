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
