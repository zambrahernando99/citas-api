# Automatizaciones n8n

## HECHOS

- WF-001: recordatorio programado de citas `APPROVED` próximas, sin duplicados y con manejo de indisponibilidad API. Fuente: brief WF-001.
- WF-002: webhook desde la API para aprobación/rechazo especializada, reprogramación y cancelación; requiere payload validado, respuesta determinista y trazabilidad. Fuente: brief WF-002.
- WF-003 es opcional: resumen diario por sede, estado y especialidad. Fuente: brief WF-003.
- Los JSON exportados se versionan en `citas-api/automations/n8n/` y no contienen credenciales. Fuente: restricciones técnicas y evidencias.

## PREGUNTAS ABIERTAS

- Contrato exacto de los endpoints y webhook, autenticación, política de reintento e idempotencia.
- Estrategia para evitar recordatorios duplicados.
