# RAW — inventario de fuentes curadas

Este directorio es append-only para fuentes aprobadas. Cada snapshot se copia desde el origen indicado y no se modifica durante INGEST; el conocimiento se sintetiza en `../wiki/`. No se copian secretos, `.env`, credenciales ni datos no públicos.

## Corte inicial v1 — 2026-09-17

| Snapshot inmutable | Origen | Uso |
| --- | --- |
| `workspace-readme.v1.md` | `../../../../../README.md` | alcance del workspace y repositorios |
| `prd.v1.md` | `../../../../../PRD.md` | producto, reglas y pantallas |
| `restricciones-tecnicas.v1.md` | `../../../../../RESTRICCIONES_TECNICAS.md` | arquitectura y seguridad |
| `requisitos-normalizacion-3fn.v1.md` | `../../../../../database/REQUISITOS_NORMALIZACION_3FN.md` | requisitos de diseño relacional |
| `evidencias-trazabilidad.v1.md` | `../../../../../EVIDENCIAS_Y_TRAZABILIDAD.md` | evidencia de evaluación |
| `wf-001-brief.v1.md` | `../../../../automations/n8n/WF-001-appointment-reminders.md` | brief WF-001 |
| `wf-002-brief.v1.md` | `../../../../automations/n8n/WF-002-status-notifications.md` | brief WF-002 |
| `wf-003-brief.v1.md` | `../../../../automations/n8n/WF-003-daily-operational-summary.md` | brief WF-003 opcional |

`database/reference/` queda explícitamente excluido del corte inicial: es solución de referencia del trainer y no dirige el diseño inicial del estudiante.
