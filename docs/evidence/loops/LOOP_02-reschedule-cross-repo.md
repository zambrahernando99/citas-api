# LOOP 02 — Guiado avanzado: reprogramación completa (cross-repo)

| Campo | Valor |
|---|---|
| Reglas innegociables | Original vigente mientras PENDING; nueva franja retenida; APPROVED libera anterior y confirma nueva; REJECTED libera la nueva y conserva la original; solo ADMIN decide; frontend refleja estado y motivo |
| Presupuesto | Máximo 4 iteraciones |
| Builder | Subagente; en esta iteración solo `citas-web/src/**` (backend ya cubierto por LOOP_01 y commit `1744813`) |
| Verifier | Subagente aislado de solo lectura: HU/DoD, `git diff`, contrato vs `AppointmentFlowController`, `npm run lint`, `npm test`, `mvn test` |
| Escalamiento | Nueva dependencia, migración o cambio de contrato → humano |

## Iteración 1
- **Builder:** reprogramación con IDs reales; badge y bloqueo de acciones cuando hay PENDING; motivo de decisión; errores 409 en `role="alert"`; panel de historial; limpieza de `types.ts`; 4 pruebas Vitest.
- **Resultados:** backend `AppointmentLifecycleIntegrationTest` 14/14; web `npm test` 4/4; `tsc` solo con 3 errores conocidos de regímenes en `CatalogScreens.tsx` (alcance de LOOP_03).
- **Verifier:** `VERDICT: PASS`.
- **Nota del orquestador:** el Verifier atribuyó al Builder `http.ts`, los servicios y `verify-s3.ps1`; esos cambios los hizo el orquestador antes del loop.

**Resultado:** COMPLETED en 1/4 iteraciones.
