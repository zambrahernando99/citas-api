# WF-001 — Recordatorio de citas próximas

**Trigger:** Schedule.

**Objetivo:** consultar citas `APPROVED` dentro de una ventana configurable (ej. próximas 24 h), enviar Gmail al usuario ficticio/de laboratorio y registrar resultado.

## Requisitos
- no enviar recordatorio a CANCELLED/REJECTED;
- evitar duplicado para la misma cita/ventana según estrategia del estudiante;
- manejar API no disponible;
- credenciales fuera del JSON;
- ejecución de prueba controlada antes de activar.

## Entregable
`WF-001-appointment-reminders.json`.

## Implementación (S5, 2026-10-01)

- Contrato API: `GET /api/v1/automation/reminders/due` y `POST /api/v1/automation/reminders/{id}/deliveries` con `X-Automation-Key` (wiki `contratos-rest.md`, DEC-013..017).
- Duplicados: un registro por cita y ventana; `SENT` definitivo, `FAILED` hasta 3 intentos.
- API caída: 3 reintentos y rama `API no disponible` sin envíos.
- Workflow en la instancia: `Hernando-WF-001-appointment-reminders` (id `0LEn366VVamn82Ix`), inactivo y sin credenciales, probado por MCP (ejecución 75).
- Flujo: `Cada hora → Config → Consultar citas APPROVED → Separar citas → Enviar recordatorio Gmail → Registrar SENT | Registrar FAILED`.

### Puesta en marcha

1. En `.env`: `AUTOMATION_API_KEY` con 32+ caracteres aleatorios; `docker compose up -d citas-api-dev` y arrancar la API.
2. Túnel: `ngrok http 8080 --traffic-policy-file automations/ngrok/traffic-policy.yml`.
3. En n8n: importar el JSON; crear la credencial *Header Auth* (nombre `X-Automation-Key`, valor = la clave) en los 3 nodos HTTP, y cambiar el nodo Gmail a OAuth2 con tu credencial propia.
4. En el nodo `Config`: `apiBaseUrl` = URL https de ngrok y `testRecipient` = tu buzón de laboratorio.
5. Ejecución manual con citas sintéticas; repetir para comprobar que no duplica; parar la API para ver la rama de error. Solo entonces activar.
