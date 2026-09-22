---
id: HU-016
tipo: historia-de-usuario
titulo: Consultar calendario profesional
estado: Aprobada
epica: "[[EP-003-oferta-profesional-y-disponibilidad]]"
esfuerzo: Bajo
sprint_sugerido: S4
dependencias: ["[[HU-014-crear-bloques-disponibilidad]]"]
relacionadas: ["[[HU-015-modificar-bloques-futuros]]"]
---
# HU-016 — Consultar calendario profesional
## Historia de usuario
**COMO** PROFESSIONAL **QUIERO** consultar mi calendario de bloques **PARA** administrar mi disponibilidad publicada.
## Contexto y descripción
Lectura propia por fecha/rango con sede y franja.
## Alcance
- Calendario de bloques propios.
## Fuera de alcance
- Calendario de otros profesionales.
## Reglas de negocio
- Ownership obligatorio.
## Dependencias y relaciones
- Épica: [[EP-003-oferta-profesional-y-disponibilidad]]
- Dependencias: [[HU-014-crear-bloques-disponibilidad]].
- Relacionadas: [[HU-015-modificar-bloques-futuros]].
## Esfuerzo
**Nivel:** Bajo. **Justificación de dificultad:** lectura filtrada de datos propios.
## Tareas de desarrollo
- [ ] **T-01 — Documentar filtros.** Dificultad: Bajo. Definir fecha/rango y proyección.
- [ ] **T-02 — Implementar consulta/UI.** Dificultad: Bajo. Aplicar ownership y pruebas.
## Criterios de aceptación
### CA-01 — Consulta propia
**Dado** PROFESSIONAL **cuando** consulta calendario **entonces** ve sus bloques con sede/franja.
### CA-02 — Filtro
**Dado** un filtro válido **cuando** consulta **entonces** devuelve el período solicitado.
### CA-03 — Aislamiento
**Dado** bloque ajeno **cuando** consulta **entonces** no se expone.
## Definition of Done
- [ ] CA-01 a CA-03 con evidencia.
- [ ] Contrato/UI y pruebas de filtro/ownership disponibles.
- [ ] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Pendiente | — | No implementada |
| DoD | Pendiente | — | — |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
## Notas y decisiones
- No incluye citas; esas pertenecen a [[HU-025-consultar-agenda-profesional]].
