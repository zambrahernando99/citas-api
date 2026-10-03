# WF-002 — Notificación por cambio de estado

**Trigger:** Webhook recibido desde `citas-api`.

Eventos mínimos:
- cita especializada APPROVED/REJECTED;
- reprogramación APPROVED/REJECTED;
- cancelación.

## Requisitos
- validar payload mínimo;
- ramificar por tipo/estado;
- Gmail con mensaje coherente;
- respuesta webhook determinista;
- error/reintento/trazabilidad;
- secretos/credenciales fuera del JSON.

## Entregable
`WF-002-status-notifications.json`.

## Implementación (S6, 2026-10-01)

- Backend: outbox `automation_event_outbox` (V9) escrito en la misma transacción que la transición; despachador cada 30 s con reintentos 1/5/15/60 min (máx. 5). Contrato en wiki `contratos-rest.md` y DEC-018.
- Workflow `Hernando-WF-002-status-notifications` (id `lLZQOsvQpmVLXYah`): Webhook Header Auth → Normalizar y validar → ¿válido? → Switch por tipo/estado (5 ramas con mensaje propio) → Gmail → 200; Gmail falla → 503; inválido → 400.
- Puesta en marcha: credencial Header Auth `X-Citas-Webhook-Secret` en el webhook, Gmail en OAuth2 con tu credencial, `testRecipient` con tu buzón, y en `.env` `N8N_STATUS_WEBHOOK_URL` (URL de producción del webhook) y `N8N_STATUS_WEBHOOK_SECRET`. Activar solo tras una prueba con `/webhook-test/`.
