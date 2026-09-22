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
- [ ] **T-01 — Resolver Medicina General/contrato.** Dificultad: Medio. Documentar conflicto de slot.
- [ ] **T-02 — Implementar reserva transaccional.** Dificultad: Alto. Revalidar y auditar.
## Criterios de aceptación
### CA-01 — Autoaprobación
**Dado** franja disponible **cuando** USER confirma general **entonces** crea cita `APPROVED`.
### CA-02 — Conflicto
**Dado** franja tomada/retenida **cuando** confirma **entonces** se rechaza sin segunda reserva.
### CA-03 — Trazabilidad
**Dado** cita creada **cuando** termina operación **entonces** sus slots y estado quedan auditados.
## Definition of Done
- [ ] CA-01 a CA-03 con evidencia.
- [ ] Contrato/UI, pruebas de concurrencia/conflicto y auditoría disponibles.
- [ ] Medicina General decidida; trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Pendiente | — | Decisión pendiente |
| DoD | Pendiente | — | — |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
## Notas y decisiones
- No se implementa hasta aprobar definición de Medicina General.
