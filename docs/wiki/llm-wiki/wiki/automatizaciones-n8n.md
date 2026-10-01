# Automatizaciones n8n

## HECHOS

- WF-001: recordatorio programado de citas `APPROVED` próximas, sin duplicados y con manejo de indisponibilidad API. Fuente: brief WF-001.
- WF-002: webhook desde la API para aprobación/rechazo especializada, reprogramación y cancelación; requiere payload validado, respuesta determinista y trazabilidad. Fuente: brief WF-002.
- WF-003 es opcional: resumen diario por sede, estado y especialidad. Fuente: brief WF-003.
- Los JSON exportados se versionan en `citas-api/automations/n8n/` y no contienen credenciales. Fuente: restricciones técnicas y evidencias.

## HECHOS S5 (2026-10-01)

- Contrato de WF-001 implementado en `citas-api` (ver [contratos REST](contratos-rest.md), DEC-013..017): `GET /api/v1/automation/reminders/due` y `POST /api/v1/automation/reminders/{id}/deliveries`, autenticados con `X-Automation-Key`.
- Duplicados: se evitan por `(cita, ventana)` en `appointment_reminder_delivery`; `FAILED` se reintenta hasta 3 veces.
- API no disponible: el nodo HTTP reintenta 3 veces y luego toma la rama de error `API no disponible` (Stop and Error) sin enviar correos.
- JSON versionado: `automations/n8n/WF-001-appointment-reminders.json` con `active: false`, sin `pinData` y con credenciales solo por nombre (`citas-api X-Automation-Key`, `Gmail OAuth2 (gmail.send)`). `scripts/verify-n8n-export.ps1` lo valida en el pre-commit.
- Acceso desde n8n remoto: ngrok con `automations/ngrok/traffic-policy.yml`, que niega todo fuera de `/api/v1/automation/`.
- Servidor MCP de la instancia: `https://impulso-n8n.aiacademy.com.co/mcp-server/http` (dato del usuario).

## HECHOS S6 (2026-10-01)

- WF-002 `Hernando-WF-002-status-notifications` (id `lLZQOsvQpmVLXYah`): webhook `POST /webhook/hernando-citas-status` con Header Auth → validación → Switch por tipo/estado (5 ramas) → Gmail → `Respond to Webhook` 200/400/503. Probado por MCP con datos simulados: rama de reprogramación rechazada y payload inválido → 400 sin Gmail.
- WF-003 `Hernando-WF-003-daily-operational-summary` (id `Z8YOsBjSqA39K1pu`): Schedule 20:00 Bogotá → API diaria → Code que agrupa por sede, estado y especialidad → Gmail; rama de incidencia si la API falla. Probado por MCP con 5 citas simuladas.
- Ambos inactivos y sin credenciales (DEC-020). JSON en `automations/n8n/`.

## PREGUNTAS ABIERTAS

- Ninguna de contrato. Falta la ejecución real con credenciales propias (Gmail OAuth, Header Auth) y el webhook activo.

## PENDIENTE DE EJECUCIÓN S5

- El entregable funcional requiere una instancia n8n, MCP operativo, acceso al API desde n8n y credenciales Gmail individuales, según `GUIA_SESIONES_S2_S6.md`.
- En la sesión del 2026-09-29 no hay herramientas MCP de n8n disponibles; tampoco se confirmó instancia ni credenciales. No se exportó un JSON incompleto ni se activó un flujo. Se requiere ejecutar e importar/exportar con la instancia real para demostrar el workflow, la invocación MCP, idempotencia y respuesta controlada.
- 2026-10-01: backend, JSON y hook listos. Siguen pendientes la importación y ejecución controlada en la instancia, las credenciales Gmail OAuth propias, el túnel ngrok activo y la invocación MCP desde un agente con el conector n8n habilitado.
- Hasta tener esa evidencia, no presentar S5 como completado. Mantener credenciales, tokens y OAuth fuera de los JSON versionados.
