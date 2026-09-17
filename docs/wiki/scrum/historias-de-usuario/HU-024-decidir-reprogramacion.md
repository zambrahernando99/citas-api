---
id: HU-024
tipo: historia-de-usuario
titulo: Decidir reprogramación
estado: Pendiente de aprobación
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
- [ ] **T-01 — Documentar ramas.** Dificultad: Alto. Precisar motivo aplicable.
- [ ] **T-02 — Implementar intercambio.** Dificultad: Alto. Actualizar cita, reservas y auditoría.
## Criterios de aceptación
### CA-01 — Aprobar
**Dado** `PENDING` **cuando** ADMIN aprueba **entonces** libera antigua, asigna nueva y actualiza cita.
### CA-02 — Rechazar
**Dado** `PENDING` **cuando** ADMIN rechaza **entonces** libera nueva y conserva original.
### CA-03 — Protección
**Dado** solicitud resuelta/no ADMIN **cuando** decide **entonces** no se altera ninguna franja.
## Definition of Done
- [ ] CA-01 a CA-03 con evidencia.
- [ ] Contrato/UI, pruebas de ambas ramas/consistencia/auditoría disponibles.
- [ ] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Pendiente | — | No implementada |
| DoD | Pendiente | — | — |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
## Notas y decisiones
- Motivo se alinea con la política de rechazo aprobada.
