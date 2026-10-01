# WF-003 — Caso adicional: resumen operativo diario

**Uso:** actividad extra si el grupo avanza rápido.

**Trigger:** Schedule.

**Flujo:** API → citas del día → agrupar por sede/estado/especialidad → construir resumen → Gmail.

## Resultado esperado
Un correo de laboratorio con métricas simples:
- total por sede;
- APPROVED/COMPLETED/NO_SHOW/CANCELLED;
- distribución por especialidad;
- incidencias de API si existen.

No requiere información privada real.

## Implementación (S6, 2026-10-01)

- Backend: `GET /api/v1/automation/appointments/daily?date=` con `X-Automation-Key`, sin datos personales (DEC-019).
- Workflow `Hernando-WF-003-daily-operational-summary` (id `Z8YOsBjSqA39K1pu`): Schedule 20:00 America/Bogota → Config → API (3 reintentos) → Code agrupa por sede, estado y especialidad → Gmail; si la API falla, correo de incidencia.
- Puesta en marcha: `apiBaseUrl` (ngrok) y `recipient` en `Config resumen`, credencial Header Auth `X-Automation-Key` en `Consultar citas del día`, Gmail en OAuth2 con tu credencial.
