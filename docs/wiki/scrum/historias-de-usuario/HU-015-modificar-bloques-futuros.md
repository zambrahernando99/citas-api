---
id: HU-015
tipo: historia-de-usuario
titulo: Modificar bloques futuros
estado: Pendiente de aprobación
epica: "[[EP-003-oferta-profesional-y-disponibilidad]]"
esfuerzo: Medio
sprint_sugerido: S4
dependencias: ["[[HU-014-crear-bloques-disponibilidad]]"]
relacionadas: ["[[HU-016-consultar-calendario-profesional]]"]
---
# HU-015 — Modificar bloques futuros
## Historia de usuario
**COMO** PROFESSIONAL **QUIERO** editar/eliminar bloques futuros no comprometidos **PARA** corregir mi disponibilidad.
## Contexto y descripción
No altera bloques ajenos, pasados o con citas comprometidas.
## Alcance
- Actualizar/eliminar bloque propio elegible.
## Fuera de alcance
- Mover compromisos de citas.
## Reglas de negocio
- Mantiene no pasado, sede asignada y no solapamiento.
## Dependencias y relaciones
- Épica: [[EP-003-oferta-profesional-y-disponibilidad]]
- Dependencias: [[HU-014-crear-bloques-disponibilidad]].
- Relacionadas: [[HU-016-consultar-calendario-profesional]].
## Esfuerzo
**Nivel:** Medio. **Justificación de dificultad:** debe detectar compromisos y preservar consistencia.
## Tareas de desarrollo
- [ ] **T-01 — Definir compromiso.** Dificultad: Medio. Consultar citas/reservas afectadas.
- [ ] **T-02 — Implementar cambio seguro.** Dificultad: Medio. Revalidar reglas temporales.
## Criterios de aceptación
### CA-01 — Edición propia
**Dado** bloque futuro propio no comprometido **cuando** lo edita/elimina **entonces** se actualiza disponibilidad.
### CA-02 — Bloque protegido
**Dado** bloque comprometido/pasado **cuando** se modifica **entonces** se rechaza.
### CA-03 — Revalidación
**Dado** edición que solapa o cambia a sede ajena **cuando** se guarda **entonces** se rechaza.
## Definition of Done
- [ ] CA-01 a CA-03 con evidencia.
- [ ] Contrato, pruebas de compromiso/reglas y actualización de disponibilidad disponibles.
- [ ] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Pendiente | — | No implementada |
| DoD | Pendiente | — | — |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
## Notas y decisiones
- "Comprometida" se valida contra reserva/cita aplicable.
