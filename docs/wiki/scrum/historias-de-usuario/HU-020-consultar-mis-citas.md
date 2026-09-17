---
id: HU-020
tipo: historia-de-usuario
titulo: Consultar mis citas
estado: Pendiente de aprobación
epica: "[[EP-004-reserva-y-consulta-de-citas]]"
esfuerzo: Bajo
sprint_sugerido: S4
dependencias: ["[[HU-018-reservar-cita-general]]", "[[HU-019-solicitar-cita-especializada]]"]
relacionadas: ["[[HU-021-cancelar-cita]]", "[[HU-022-solicitar-reprogramacion]]"]
---
# HU-020 — Consultar mis citas
## Historia de usuario
**COMO** USER **QUIERO** consultar y filtrar mis citas **PARA** conocer estado y detalles.
## Contexto y descripción
Muestra sede, profesional, especialidad, fecha/hora, duración, estado y motivo de rechazo cuando existe.
## Alcance
- Listado/detalle propio por estado/fecha.
## Fuera de alcance
- Consultar citas de otros USER.
## Reglas de negocio
- Ownership; auditoría no se expone como CRUD.
## Dependencias y relaciones
- Épica: [[EP-004-reserva-y-consulta-de-citas]]
- Dependencias: [[HU-018-reservar-cita-general]], [[HU-019-solicitar-cita-especializada]].
- Relacionadas: [[HU-021-cancelar-cita]], [[HU-022-solicitar-reprogramacion]].
## Esfuerzo
**Nivel:** Bajo. **Justificación de dificultad:** proyección filtrada con ownership.
## Tareas de desarrollo
- [ ] **T-01 — Definir proyección/filtros.** Dificultad: Bajo. Incluir campos mínimos.
- [ ] **T-02 — Implementar lectura/UI.** Dificultad: Medio. Aislar usuarios y probar filtros.
## Criterios de aceptación
### CA-01 — Datos mínimos
**Dado** USER **cuando** consulta citas **entonces** ve los campos obligatorios y motivo si existe.
### CA-02 — Filtros
**Dado** filtro estado/fecha **cuando** consulta **entonces** devuelve sus coincidencias.
### CA-03 — Aislamiento
**Dado** cita ajena **cuando** consulta **entonces** no se expone.
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
- No expone historial como CRUD.
