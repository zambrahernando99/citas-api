# Automatizaciones n8n

## HECHOS

- WF-001: recordatorio programado de citas `APPROVED` próximas, sin duplicados y con manejo de indisponibilidad API. Fuente: brief WF-001.
- WF-002: webhook desde la API para aprobación/rechazo especializada, reprogramación y cancelación; requiere payload validado, respuesta determinista y trazabilidad. Fuente: brief WF-002.
- WF-003 es opcional: resumen diario por sede, estado y especialidad. Fuente: brief WF-003.
- Los JSON exportados se versionan en `citas-api/automations/n8n/` y no contienen credenciales. Fuente: restricciones técnicas y evidencias.

## PREGUNTAS ABIERTAS

- Contrato exacto de los endpoints y webhook, autenticación, política de reintento e idempotencia.
- Estrategia para evitar recordatorios duplicados.

## PENDIENTE DE EJECUCIÓN S5

- El entregable funcional requiere una instancia n8n, MCP operativo, acceso al API desde n8n y credenciales Gmail individuales, según `GUIA_SESIONES_S2_S6.md`.
- En la sesión del 2026-09-29 no hay herramientas MCP de n8n disponibles; tampoco se confirmó instancia ni credenciales. No se exportó un JSON incompleto ni se activó un flujo. Se requiere ejecutar e importar/exportar con la instancia real para demostrar el workflow, la invocación MCP, idempotencia y respuesta controlada.
- Hasta conectar el trainer, no presentar S5 como completado. Mantener credenciales, tokens y OAuth fuera de los JSON versionados.
