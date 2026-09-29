---
id: HU-018
tipo: historia-de-usuario
titulo: Reservar cita general
estado: Aprobada
epica: "[[EP-004-reserva-y-consulta-de-citas]]"
esfuerzo: Alto
sprint_sugerido: S4
dependencias: ["[[HU-017-buscar-disponibilidad]]", "[[HU-028-auditar-cambios-de-estado]]"]
relacionadas: ["[[HU-020-consultar-mis-citas]]"]
---
# HU-018 — Reservar cita general
## Historia de usuario
**COMO** USER **QUIERO** confirmar una cita de Medicina General **PARA** obtener una cita aprobada automáticamente.
## Contexto y descripción
Confirma profesional general y franja disponible; la definición de Medicina General está pendiente.
## Alcance
- Reserva general en `APPROVED`.
## Fuera de alcance
- Decisión ADMIN.
## Reglas de negocio
- Revalidar disponibilidad; sin doble reserva; auditar estado.
## Dependencias y relaciones
- Épica: [[EP-004-reserva-y-consulta-de-citas]]
- Dependencias: [[HU-017-buscar-disponibilidad]], [[HU-028-auditar-cambios-de-estado]].
- Relacionadas: [[HU-020-consultar-mis-citas]].
## Esfuerzo
**Nivel:** Alto. **Justificación de dificultad:** reserva concurrente, estado y auditoría.
## Tareas de desarrollo
- [x] **T-01 — Resolver Medicina General/contrato.** Dificultad: Medio. Documentar conflicto de slot.
- [x] **T-02 — Implementar reserva transaccional.** Dificultad: Alto. Revalidar y auditar.
## Criterios de aceptación
### CA-01 — Autoaprobación
**Dado** franja disponible **cuando** USER confirma general **entonces** crea cita `APPROVED`.
### CA-02 — Conflicto
**Dado** franja tomada/retenida **cuando** confirma **entonces** se rechaza sin segunda reserva.
### CA-03 — Trazabilidad
**Dado** cita creada **cuando** termina operación **entonces** sus slots y estado quedan auditados.
## Definition of Done
- [x] CA-01 a CA-03 con evidencia.
- [x] Contrato/UI, pruebas de concurrencia/conflicto y auditoría disponibles.
- [x] Medicina General decidida; trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Validado backend | `AppointmentLifecycleIntegrationTest#generalAppointmentIsApprovedAndSameSlotCannotBeBookedTwice` | APPROVED directo; sin doble reserva |
| UI | Validado | `ReconciliationScreens.test.tsx` | Reserva general APPROVED |
| E2E | Validado | `docs/evidence/s3-s4/03-e2e-docker.md` | Doble reserva 409 |
| DoD | Validado | `docs/evidence/s3-s4/01-red-green.md`, `02-hook.md`, `docs/evidence/s3-s4/03-e2e-docker.md` | Backend 40 pruebas, web 13 Vitest, E2E 47/47 |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
- 2026-09-29 — Validada en S3/S4: evidencia en docs/evidence/s3-s4/ y docs/evidence/loops/.
## Notas y decisiones
- Medicina General se reserva como cita general APPROVED.
