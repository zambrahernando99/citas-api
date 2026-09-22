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
- [ ] **T-01 — Definir proyección.** Dificultad: Medio. Separar tipos/estados y campos de decisión.
- [ ] **T-02 — Implementar consulta/UI.** Dificultad: Medio. Aplicar filtros/rol y pruebas.
## Criterios de aceptación
### CA-01 — Pendientes
**Dado** ADMIN **cuando** abre bandeja **entonces** ve `REQUESTED` y `PENDING`.
### CA-02 — Filtros
**Dado** filtros del PRD **cuando** aplica **entonces** ve coincidencias.
### CA-03 — No efecto
**Dado** un ítem **cuando** solo se consulta **entonces** no cambia estado.
## Definition of Done
- [ ] CA-01 a CA-03 con evidencia.
- [ ] Contrato/UI y pruebas de rol/tipos/filtros disponibles.
- [ ] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Pendiente | — | No implementada |
| DoD | Pendiente | — | — |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
## Notas y decisiones
- Enlaza a las HUs de decisión, no las sustituye.
