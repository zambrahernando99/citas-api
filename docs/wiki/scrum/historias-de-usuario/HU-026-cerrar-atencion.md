---
id: HU-026
tipo: historia-de-usuario
titulo: Cerrar atención
estado: Aprobada
epica: "[[EP-006-operacion-y-auditoria]]"
esfuerzo: Medio
sprint_sugerido: S6
dependencias: ["[[HU-025-consultar-agenda-profesional]]", "[[HU-028-auditar-cambios-de-estado]]"]
relacionadas: []
---
# HU-026 — Cerrar atención
## Historia de usuario
**COMO** PROFESSIONAL **QUIERO** marcar una cita propia como `COMPLETED` o `NO_SHOW` **PARA** cerrar la atención.
## Contexto y descripción
Solo aplica a cita `APPROVED` pasada/aplicable, definición que requiere decisión.
## Alcance
- Dos transiciones de cierre propias.
## Fuera de alcance
- Editar historia clínica o resolver corrección no aprobada.
## Reglas de negocio
- Registrar historial con actor/fuente; ownership.
## Dependencias y relaciones
- Épica: [[EP-006-operacion-y-auditoria]]
- Dependencias: [[HU-025-consultar-agenda-profesional]], [[HU-028-auditar-cambios-de-estado]].
- Relacionadas: ninguna.
## Esfuerzo
**Nivel:** Medio. **Justificación de dificultad:** transición y elegibilidad temporal pendiente.
## Tareas de desarrollo
- [ ] **T-01 — Decidir elegibilidad.** Dificultad: Medio. Precisar pasada/aplicable y corrección.
- [ ] **T-02 — Implementar cierre.** Dificultad: Medio. Validar ownership y auditar.
## Criterios de aceptación
### CA-01 — Cierre válido
**Dado** cita propia aplicable `APPROVED` **cuando** marca resultado **entonces** queda `COMPLETED` o `NO_SHOW`.
### CA-02 — Protección
**Dado** cita ajena/no aplicable **cuando** cierra **entonces** no cambia.
### CA-03 — Auditoría
**Dado** cierre válido **cuando** concluye **entonces** registra actor/fuente/fecha.
## Definition of Done
- [ ] CA-01 a CA-03 con evidencia.
- [ ] Decisión temporal, contrato/UI y pruebas de transición/ownership/auditoría disponibles.
- [ ] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Pendiente | — | No implementada |
| DoD | Pendiente | — | Elegibilidad pendiente |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
## Notas y decisiones
- Requiere decisión de “pasada/aplicable”.
