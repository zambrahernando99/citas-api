---
id: HU-028
tipo: historia-de-usuario
titulo: Auditar cambios de estado
estado: Pendiente de aprobación
epica: "[[EP-006-operacion-y-auditoria]]"
esfuerzo: Alto
sprint_sugerido: S4
dependencias: ["[[HU-004-autorizar-por-rol-y-propiedad]]"]
relacionadas: ["[[HU-018-reservar-cita-general]]", "[[HU-019-solicitar-cita-especializada]]", "[[HU-021-cancelar-cita]]", "[[HU-023-decidir-cita-especializada]]", "[[HU-024-decidir-reprogramacion]]", "[[HU-026-cerrar-atencion]]"]
---
# HU-028 — Auditar cambios de estado
## Historia de usuario
**COMO** sistema **QUIERO** conservar un historial no editable de cada cambio de estado **PARA** asegurar trazabilidad verificable.
## Contexto y descripción
Registra cita, estado nuevo, actor cuando existe, fuente SYSTEM/USER/ADMIN, fecha/hora y motivo opcional.
## Alcance
- Escritura interna de historial y lectura solo cuando una HU autorizada la requiere.
## Fuera de alcance
- CRUD general/modificación de auditoría.
## Reglas de negocio
- Todo cambio se audita; no expone secretos ni datos no autorizados.
## Dependencias y relaciones
- Épica: [[EP-006-operacion-y-auditoria]]
- Dependencias: [[HU-004-autorizar-por-rol-y-propiedad]].
- Relacionadas: [[HU-018-reservar-cita-general]], [[HU-019-solicitar-cita-especializada]], [[HU-021-cancelar-cita]], [[HU-023-decidir-cita-especializada]], [[HU-024-decidir-reprogramacion]], [[HU-026-cerrar-atencion]].
## Esfuerzo
**Nivel:** Alto. **Justificación de dificultad:** es transversal y debe preservar inmutabilidad/privacidad.
## Tareas de desarrollo
- [ ] **T-01 — Modelar historial.** Dificultad: Alto. Mantener 3FN e inmutabilidad; Flyway si aplica.
- [ ] **T-02 — Integrar transiciones.** Dificultad: Alto. Conectar casos de uso y pruebas de trazabilidad.
## Criterios de aceptación
### CA-01 — Datos de auditoría
**Dado** cambio de estado **cuando** ocurre **entonces** guarda cita, estado, actor/fuente, fecha/hora y motivo opcional.
### CA-02 — Inmutabilidad
**Dado** historial creado **cuando** se intenta modificar como CRUD **entonces** no se permite.
### CA-03 — Privacidad
**Dado** lectura autorizada de cita **cuando** muestra motivo/historial aplicable **entonces** no expone secretos ni recurso ajeno.
## Definition of Done
- [ ] CA-01 a CA-03 con evidencia.
- [ ] Migración si aplica, pruebas de inmutabilidad/actor/fuente/motivo y contratos protegidos disponibles.
- [ ] Todas las HUs relacionadas invocan auditoría al cambiar estado; trazabilidad actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Pendiente | — | No implementada |
| DoD | Pendiente | — | — |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
## Notas y decisiones
- Base para transiciones; no equivale a CRUD de auditoría.
