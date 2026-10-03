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
- JSON versionado: `automations/n8n/WF-001-appointment-reminders.json` con `active: false`, sin `pinData` y sin credenciales (re-exportado desde la instancia el 2026-10-03). `scripts/verify-n8n-export.ps1` lo valida en el pre-commit.
- Acceso desde n8n remoto: ngrok con `automations/ngrok/traffic-policy.yml`, que niega todo fuera de `/api/v1/automation/`.
- Servidor MCP de la instancia: `https://impulso-n8n.aiacademy.com.co/mcp-server/http` (dato del usuario).

## HECHOS S6 (2026-10-01)

- WF-002 `Hernando-WF-002-status-notifications` (id `lLZQOsvQpmVLXYah`): webhook `POST /webhook/hernando-citas-status` con Header Auth → validación → Switch por tipo/estado (5 ramas) → Gmail → `Respond to Webhook` 200/400/503. Probado por MCP con datos simulados: rama de reprogramación rechazada y payload inválido → 400 sin Gmail.
- WF-003 `Hernando-WF-003-daily-operational-summary` (id `Z8YOsBjSqA39K1pu`): Schedule 20:00 Bogotá → API diaria → Code que agrupa por sede, estado y especialidad → Gmail; rama de incidencia si la API falla. Probado por MCP con 5 citas simuladas.
- WF-001 `Hernando-WF-001-appointment-reminders` (id `0LEn366VVamn82Ix`), creado por MCP el 2026-10-03 con el mismo prefijo; probado con 2 citas simuladas (ejecución 75).
- Los tres inactivos y sin credenciales (DEC-020). JSON en `automations/n8n/`.

## HECHOS — ejecución real (2026-10-03)

- Los tres workflows se ejecutaron contra la API real por ngrok con credenciales propias del estudiante: WF-001 envió, no duplicó y manejó la API caída; WF-002 (activo) entregó 4 eventos con HTTP 200; WF-003 envió el resumen del día. Evidencia: `docs/evidence/s6/02-ejecucion-real.md` con capturas.
- WF-002 queda activo; WF-001 y WF-003 inactivos hasta que el estudiante decida activarlos.

## PREGUNTAS ABIERTAS

- Ninguna.
