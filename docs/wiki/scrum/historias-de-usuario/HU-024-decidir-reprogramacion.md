---
id: HU-024
tipo: historia-de-usuario
titulo: Decidir reprogramación
estado: Aprobada
epica: "[[EP-005-ciclo-de-vida-de-citas]]"
esfuerzo: Alto
sprint_sugerido: S5
dependencias: ["[[HU-022-solicitar-reprogramacion]]", "[[HU-028-auditar-cambios-de-estado]]"]
relacionadas: ["[[HU-027-consultar-bandeja-administrativa]]"]
---
# HU-024 — Decidir reprogramación
## Historia de usuario
**COMO** ADMIN **QUIERO** aprobar o rechazar reprogramación pendiente **PARA** actualizar la cita sin perder la franja original indebidamente.
## Contexto y descripción
Al aprobar intercambia franja; al rechazar libera nueva y preserva original.
## Alcance
- Decisión de `PENDING`.
## Fuera de alcance
- Cambiar profesional/especialidad.
## Reglas de negocio
- USER conserva cita tras rechazo o puede cancelarla.
## Dependencias y relaciones
- Épica: [[EP-005-ciclo-de-vida-de-citas]]
- Dependencias: [[HU-022-solicitar-reprogramacion]], [[HU-028-auditar-cambios-de-estado]].
- Relacionadas: [[HU-027-consultar-bandeja-administrativa]].
## Esfuerzo
**Nivel:** Alto. **Justificación de dificultad:** intercambio transaccional de reservas.
## Tareas de desarrollo
- [x] **T-01 — Documentar ramas.** Dificultad: Alto. Precisar motivo aplicable.
- [x] **T-02 — Implementar intercambio.** Dificultad: Alto. Actualizar cita, reservas y auditoría.
## Criterios de aceptación
### CA-01 — Aprobar
**Dado** `PENDING` **cuando** ADMIN aprueba **entonces** libera antigua, asigna nueva y actualiza cita.
### CA-02 — Rechazar
**Dado** `PENDING` **cuando** ADMIN rechaza **entonces** libera nueva y conserva original.
### CA-03 — Protección
**Dado** solicitud resuelta/no ADMIN **cuando** decide **entonces** no se altera ninguna franja.
## Definition of Done
- [x] CA-01 a CA-03 con evidencia.
- [x] Contrato/UI, pruebas de ambas ramas/consistencia/auditoría disponibles.
- [x] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Validado backend | `AppointmentLifecycleIntegrationTest#rescheduleKeepsOriginalWhilePendingRejectReleasesNewAndApproveSwaps` | Aprobar intercambia; rechazar libera nuevo |
| Loops | Validado | `docs/evidence/loops/LOOP_02-*.md` | — |
| UI | Validado | `docs/evidence/s3-s4/03-e2e-docker.md` | Aprobación desde bandeja |
| DoD | Validado | `docs/evidence/s3-s4/01-red-green.md`, `02-hook.md`, `docs/evidence/s3-s4/03-e2e-docker.md` | Backend 40 pruebas, web 13 Vitest, E2E 47/47 |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
- 2026-09-29 — Validada en S3/S4: evidencia en docs/evidence/s3-s4/ y docs/evidence/loops/.
## Notas y decisiones
- Motivo se alinea con la política de rechazo aprobada.
