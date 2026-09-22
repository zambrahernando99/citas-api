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
