---
id: HU-027
tipo: historia-de-usuario
titulo: Consultar bandeja administrativa
estado: Aprobada
epica: "[[EP-006-operacion-y-auditoria]]"
esfuerzo: Medio
sprint_sugerido: S6
dependencias: ["[[HU-019-solicitar-cita-especializada]]", "[[HU-022-solicitar-reprogramacion]]"]
relacionadas: ["[[HU-023-decidir-cita-especializada]]", "[[HU-024-decidir-reprogramacion]]"]
---
# HU-027 — Consultar bandeja administrativa
## Historia de usuario
**COMO** ADMIN **QUIERO** consultar solicitudes pendientes filtrables **PARA** decidirlas con prioridad.
## Contexto y descripción
Incluye especializadas `REQUESTED` y reprogramaciones `PENDING` por sede, profesional, especialidad y fecha.
## Alcance
- Bandeja de pendientes y filtros.
## Fuera de alcance
- Cambiar estado al solo consultar.
## Reglas de negocio
- No mezcla citas generales autoaprobadas; solo ADMIN.
## Dependencias y relaciones
- Épica: [[EP-006-operacion-y-auditoria]]
- Dependencias: [[HU-019-solicitar-cita-especializada]], [[HU-022-solicitar-reprogramacion]].
- Relacionadas: [[HU-023-decidir-cita-especializada]], [[HU-024-decidir-reprogramacion]].
## Esfuerzo
**Nivel:** Medio. **Justificación de dificultad:** combina tipos pendientes y filtros protegidos.
## Tareas de desarrollo
- [x] **T-01 — Definir proyección.** Dificultad: Medio. Separar tipos/estados y campos de decisión.
- [x] **T-02 — Implementar consulta/UI.** Dificultad: Medio. Aplicar filtros/rol y pruebas.
## Criterios de aceptación
### CA-01 — Pendientes
**Dado** ADMIN **cuando** abre bandeja **entonces** ve `REQUESTED` y `PENDING`.
### CA-02 — Filtros
**Dado** filtros del PRD **cuando** aplica **entonces** ve coincidencias.
### CA-03 — No efecto
**Dado** un ítem **cuando** solo se consulta **entonces** no cambia estado.
## Definition of Done
- [x] CA-01 a CA-03 con evidencia.
- [x] Contrato/UI y pruebas de rol/tipos/filtros disponibles.
- [x] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Validado backend | `AppointmentLifecycleIntegrationTest#adminInboxCombinesRequestedAndPendingReschedulesWithFiltersAndNoSideEffects` | `GET /api/v1/admin/inbox` con filtros, sin efectos |
| UI | Validado | `ReconciliationScreens.test.tsx` | Bandeja |
| E2E | Validado | `docs/evidence/s3-s4/03-e2e-docker.md` | — |
| DoD | Validado | `docs/evidence/s3-s4/01-red-green.md`, `02-hook.md`, `docs/evidence/s3-s4/03-e2e-docker.md` | Backend 40 pruebas, web 13 Vitest, E2E 47/47 |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
- 2026-09-29 — Validada en S3/S4: evidencia en docs/evidence/s3-s4/ y docs/evidence/loops/.
## Notas y decisiones
- Enlaza a las HUs de decisión, no las sustituye.
