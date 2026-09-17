---
id: HU-023
tipo: historia-de-usuario
titulo: Decidir cita especializada
estado: Pendiente de aprobación
epica: "[[EP-005-ciclo-de-vida-de-citas]]"
esfuerzo: Medio
sprint_sugerido: S5
dependencias: ["[[HU-019-solicitar-cita-especializada]]", "[[HU-028-auditar-cambios-de-estado]]"]
relacionadas: ["[[HU-027-consultar-bandeja-administrativa]]"]
---
# HU-023 — Decidir cita especializada
## Historia de usuario
**COMO** ADMIN **QUIERO** aprobar o rechazar solicitud especializada pendiente **PARA** resolverla conservando disponibilidad correcta.
## Contexto y descripción
Aprueba `APPROVED`; rechazo `REJECTED` exige motivo y libera slots.
## Alcance
- Decisión única de `REQUESTED`.
## Fuera de alcance
- Decidir solicitud ya resuelta.
## Reglas de negocio
- Solo ADMIN; auditoría incluye actor/fuente/motivo.
## Dependencias y relaciones
- Épica: [[EP-005-ciclo-de-vida-de-citas]]
- Dependencias: [[HU-019-solicitar-cita-especializada]], [[HU-028-auditar-cambios-de-estado]].
- Relacionadas: [[HU-027-consultar-bandeja-administrativa]].
## Esfuerzo
**Nivel:** Medio. **Justificación de dificultad:** transición, motivo y liberación consistente.
## Tareas de desarrollo
- [ ] **T-01 — Documentar comandos/errores.** Dificultad: Medio. Explicitar motivo y segunda decisión.
- [ ] **T-02 — Implementar decisión.** Dificultad: Medio. Mantener/liberar slots y auditar.
## Criterios de aceptación
### CA-01 — Aprobación
**Dado** `REQUESTED` **cuando** ADMIN aprueba **entonces** queda `APPROVED` y mantiene slots.
### CA-02 — Rechazo
**Dado** `REQUESTED` **cuando** ADMIN rechaza con motivo **entonces** queda `REJECTED` y libera slots.
### CA-03 — Protección
**Dado** ausencia de motivo o segunda decisión **cuando** decide **entonces** se rechaza.
## Definition of Done
- [ ] CA-01 a CA-03 con evidencia.
- [ ] Contrato/UI, pruebas de ambas ramas/rol/auditoría disponibles.
- [ ] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Pendiente | — | No implementada |
| DoD | Pendiente | — | — |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
## Notas y decisiones
- Se alimenta de la bandeja administrativa.
