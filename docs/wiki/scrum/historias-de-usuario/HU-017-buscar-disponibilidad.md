---
id: HU-017
tipo: historia-de-usuario
titulo: Buscar disponibilidad
estado: Aprobada
epica: "[[EP-003-oferta-profesional-y-disponibilidad]]"
esfuerzo: Alto
sprint_sugerido: S4
dependencias: ["[[HU-010-administrar-especialidades]]", "[[HU-012-asignar-especialidades-y-sedes]]", "[[HU-013-activar-profesional]]", "[[HU-014-crear-bloques-disponibilidad]]"]
relacionadas: ["[[HU-018-reservar-cita-general]]", "[[HU-019-solicitar-cita-especializada]]"]
---
# HU-017 — Buscar disponibilidad
## Historia de usuario
**COMO** USER **QUIERO** filtrar franjas reservables **PARA** elegir una cita válida.
## Contexto y descripción
Filtros: sede, tipo, especialidad, profesional y fecha; la consulta no sustituye revalidación al reservar.
## Alcance
- Disponibilidad futura y reservable.
## Fuera de alcance
- Garantizar reserva solo por visualizarla.
## Reglas de negocio
- Excluir ocupado/retenido/inactivo; 60 min exige dos slots consecutivos.
## Dependencias y relaciones
- Épica: [[EP-003-oferta-profesional-y-disponibilidad]]
- Dependencias: [[HU-010-administrar-especialidades]], [[HU-012-asignar-especialidades-y-sedes]], [[HU-013-activar-profesional]], [[HU-014-crear-bloques-disponibilidad]].
- Relacionadas: [[HU-018-reservar-cita-general]], [[HU-019-solicitar-cita-especializada]].
## Esfuerzo
**Nivel:** Alto. **Justificación de dificultad:** integra tiempo, asignaciones, actividad, duración y reservas.
## Tareas de desarrollo
- [ ] **T-01 — Definir consulta/índices.** Dificultad: Alto. Documentar filtros y consecutividad.
- [ ] **T-02 — Implementar búsqueda/UI.** Dificultad: Alto. Probar conflictos y resultados vacíos.
## Criterios de aceptación
### CA-01 — Filtros
**Dado** filtros del PRD **cuando** USER busca **entonces** recibe franjas compatibles.
### CA-02 — Exclusiones
**Dado** slot ocupado/retenido o profesional inactivo/no asignado **cuando** busca **entonces** no aparece.
### CA-03 — Duración
**Dado** especialidad 60 **cuando** busca **entonces** solo aparecen dos slots consecutivos disponibles.
## Definition of Done
- [ ] CA-01 a CA-03 con evidencia.
- [ ] Contrato REST/UI, pruebas de filtros/30-60/conflictos y consistencia de datos disponibles.
- [ ] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Pendiente | — | No implementada |
| DoD | Pendiente | — | — |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
## Notas y decisiones
- Revalidación pertenece a la creación de reserva.
