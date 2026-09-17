---
id: HU-019
tipo: historia-de-usuario
titulo: Solicitar cita especializada
estado: Pendiente de aprobación
epica: "[[EP-004-reserva-y-consulta-de-citas]]"
esfuerzo: Alto
sprint_sugerido: S4
dependencias: ["[[HU-017-buscar-disponibilidad]]", "[[HU-028-auditar-cambios-de-estado]]"]
relacionadas: ["[[HU-023-decidir-cita-especializada]]"]
---
# HU-019 — Solicitar cita especializada
## Historia de usuario
**COMO** USER **QUIERO** solicitar cita especializada en una franja disponible **PARA** que ADMIN la decida sin doble reserva.
## Contexto y descripción
Selecciona especialidad, sede, profesional y horario; nace `REQUESTED` y retiene slots.
## Alcance
- Crear solicitud y retención.
## Fuera de alcance
- Aprobar/rechazar la solicitud.
## Reglas de negocio
- Duración aplica slots; la expiración de retención es incógnita.
## Dependencias y relaciones
- Épica: [[EP-004-reserva-y-consulta-de-citas]]
- Dependencias: [[HU-017-buscar-disponibilidad]], [[HU-028-auditar-cambios-de-estado]].
- Relacionadas: [[HU-023-decidir-cita-especializada]].
## Esfuerzo
**Nivel:** Alto. **Justificación de dificultad:** reserva concurrente y retención transaccional.
## Tareas de desarrollo
- [ ] **T-01 — Documentar solicitud/retención.** Dificultad: Alto. Definir conflicto y pendiente de expiración.
- [ ] **T-02 — Implementar creación.** Dificultad: Alto. Revalidar slots y auditar estado.
## Criterios de aceptación
### CA-01 — Estado inicial
**Dado** selección compatible disponible **cuando** USER solicita **entonces** nace en `REQUESTED`.
### CA-02 — Retención
**Dado** solicitud creada **cuando** concluye **entonces** retiene todos los slots de duración.
### CA-03 — Conflicto
**Dado** slots ocupados/retenidos **cuando** solicita **entonces** no se crea segunda reserva.
## Definition of Done
- [ ] CA-01 a CA-03 con evidencia.
- [ ] Contrato/UI, pruebas de conflicto/duración/auditoría y migración si aplica disponibles.
- [ ] Expiración documentada como decisión pendiente; trazabilidad actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Pendiente | — | No implementada |
| DoD | Pendiente | — | — |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
## Notas y decisiones
- La decisión corresponde a [[HU-023-decidir-cita-especializada]].
