---
id: HU-021
tipo: historia-de-usuario
titulo: Cancelar cita
estado: Pendiente de aprobación
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
- [ ] **T-01 — Definir elegibilidad.** Dificultad: Medio. Resolver estados terminales.
- [ ] **T-02 — Implementar transición.** Dificultad: Medio. Liberar y auditar coherentemente.
## Criterios de aceptación
### CA-01 — Cancelación válida
**Dado** cita propia futura no terminal **cuando** USER cancela **entonces** queda `CANCELLED`.
### CA-02 — Liberación
**Dado** cancelación válida **cuando** termina **entonces** libera slots correspondientes.
### CA-03 — Rechazo
**Dado** cita ajena/pasada/terminal **cuando** cancela **entonces** no cambia.
## Definition of Done
- [ ] CA-01 a CA-03 con evidencia.
- [ ] Contrato/UI, pruebas de elegibilidad/liberación/auditoría y estados terminales definidos.
- [ ] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Pendiente | — | No implementada |
| DoD | Pendiente | — | Estados pendientes |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
## Notas y decisiones
- Terminalidad requiere revisión.
