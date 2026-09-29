---
id: HU-021
tipo: historia-de-usuario
titulo: Cancelar cita
estado: Aprobada
epica: "[[EP-005-ciclo-de-vida-de-citas]]"
esfuerzo: Medio
sprint_sugerido: S5
dependencias: ["[[HU-020-consultar-mis-citas]]", "[[HU-028-auditar-cambios-de-estado]]"]
relacionadas: []
---
# HU-021 — Cancelar cita
## Historia de usuario
**COMO** USER **QUIERO** cancelar una cita futura no terminal **PARA** liberar su disponibilidad.
## Contexto y descripción
La transición es `CANCELLED`, no reactivable directamente.
## Alcance
- Cancelación propia elegible y liberación.
## Fuera de alcance
- Reactivación directa.
## Reglas de negocio
- Cancela solo futuro/no terminal; libera reserva y audita USER.
## Dependencias y relaciones
- Épica: [[EP-005-ciclo-de-vida-de-citas]]
- Dependencias: [[HU-020-consultar-mis-citas]], [[HU-028-auditar-cambios-de-estado]].
- Relacionadas: ninguna.
## Esfuerzo
**Nivel:** Medio. **Justificación de dificultad:** transición, liberación y terminalidad pendiente.
## Tareas de desarrollo
- [x] **T-01 — Definir elegibilidad.** Dificultad: Medio. Resolver estados terminales.
- [x] **T-02 — Implementar transición.** Dificultad: Medio. Liberar y auditar coherentemente.
## Criterios de aceptación
### CA-01 — Cancelación válida
**Dado** cita propia futura no terminal **cuando** USER cancela **entonces** queda `CANCELLED`.
### CA-02 — Liberación
**Dado** cancelación válida **cuando** termina **entonces** libera slots correspondientes.
### CA-03 — Rechazo
**Dado** cita ajena/pasada/terminal **cuando** cancela **entonces** no cambia.
## Definition of Done
- [x] CA-01 a CA-03 con evidencia.
- [x] Contrato/UI, pruebas de elegibilidad/liberación/auditoría y estados terminales definidos.
- [x] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Validado backend | `AppointmentLifecycleIntegrationTest#onlyOwnerCancelsFutureAppointmentAndSlotsAreReleasedWithAudit`, `#pastAppointmentCannotBeCancelled` | Libera slots y audita; pasadas rechazadas |
| E2E | Validado | `docs/evidence/s3-s4/03-e2e-docker.md` | Cancelación |
| DoD | Validado | `docs/evidence/s3-s4/01-red-green.md`, `02-hook.md`, `docs/evidence/s3-s4/03-e2e-docker.md` | Backend 40 pruebas, web 13 Vitest, E2E 47/47 |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
- 2026-09-29 — Validada en S3/S4: evidencia en docs/evidence/s3-s4/ y docs/evidence/loops/.
## Notas y decisiones
- Terminalidad requiere revisión.
