---
id: HU-025
tipo: historia-de-usuario
titulo: Consultar agenda profesional
estado: Pendiente de aprobación
epica: "[[EP-006-operacion-y-auditoria]]"
esfuerzo: Medio
sprint_sugerido: S6
dependencias: ["[[HU-018-reservar-cita-general]]", "[[HU-019-solicitar-cita-especializada]]"]
relacionadas: ["[[HU-026-cerrar-atencion]]"]
---
# HU-025 — Consultar agenda profesional
## Historia de usuario
**COMO** PROFESSIONAL **QUIERO** consultar mis citas `APPROVED` por día/semana y sede **PARA** atender mi agenda.
## Contexto y descripción
Solo presenta citas propias y datos necesarios para atención.
## Alcance
- Agenda aprobada propia con filtros.
## Fuera de alcance
- Consulta de citas ajenas o historia clínica.
## Reglas de negocio
- Ownership y mínimo dato necesario.
## Dependencias y relaciones
- Épica: [[EP-006-operacion-y-auditoria]]
- Dependencias: [[HU-018-reservar-cita-general]], [[HU-019-solicitar-cita-especializada]].
- Relacionadas: [[HU-026-cerrar-atencion]].
## Esfuerzo
**Nivel:** Medio. **Justificación de dificultad:** proyección segura por período/sede.
## Tareas de desarrollo
- [ ] **T-01 — Definir proyección.** Dificultad: Medio. Acordar campos mínimos/filtros.
- [ ] **T-02 — Implementar agenda.** Dificultad: Medio. Aplicar ownership y pruebas.
## Criterios de aceptación
### CA-01 — Agenda propia
**Dado** PROFESSIONAL **cuando** consulta **entonces** ve únicamente sus citas `APPROVED`.
### CA-02 — Filtros
**Dado** día/semana/sede **cuando** filtra **entonces** obtiene coincidencias.
### CA-03 — Privacidad
**Dado** cita de otro profesional **cuando** consulta **entonces** no se expone.
## Definition of Done
- [ ] CA-01 a CA-03 con evidencia.
- [ ] Contrato/UI y pruebas de filtros/ownership disponibles.
- [ ] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Pendiente | — | No implementada |
| DoD | Pendiente | — | — |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
## Notas y decisiones
- No implica gestión de bloques.
