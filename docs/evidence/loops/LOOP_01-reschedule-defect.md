# LOOP 01 — Guiado simple: defecto de reprogramación

| Campo | Valor |
|---|---|
| Disparador | Prueba roja `rescheduleKeepsOriginalWhilePendingRejectReleasesNewAndApproveSwaps` (500 `Data conversion error converting "BINARY to BIGINT"`) |
| Meta verificable | La prueba pasa y no aparecen nuevas fallas en `AppointmentFlowIntegrationTest` / `AppointmentLifecycleIntegrationTest` |
| Presupuesto | Máximo 3 iteraciones |
| Builder | Subagente con alcance limitado a `AppointmentFlowPersistenceAdapter` (servicio solo si era imprescindible); sin tocar esquema, UI ni pruebas |
| Verifier | Subagente aislado de solo lectura: `git diff`, HU-022/024/028, ejecución de pruebas en Docker |
| Parada | Veredicto PASS o 3 iteraciones → BLOCKED |
| Escalamiento | Migración, cambio de UI o de pruebas → humano |

## Iteración 1
- **Builder:** `getLong` → `getBytes` al detectar reprogramación PENDING; historial `APPROVED/ADMIN` con motivo "Reprogramación aprobada: …" al aprobar.
- **Pruebas:** 17 ejecutadas; la objetivo pasa; 3 fallas esperadas fuera de alcance (historial, bandeja, IDs).
- **Verifier:** `VERDICT: PASS`. Diff de 1 archivo, `src/test` intacto, criterios revisados.

**Resultado:** COMPLETED en 1/3 iteraciones.
