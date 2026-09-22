---
id: HU-022
tipo: historia-de-usuario
titulo: Solicitar reprogramación
estado: Aprobada
epica: "[[EP-005-ciclo-de-vida-de-citas]]"
esfuerzo: Alto
sprint_sugerido: S5
dependencias: ["[[HU-017-buscar-disponibilidad]]", "[[HU-020-consultar-mis-citas]]"]
relacionadas: ["[[HU-024-decidir-reprogramacion]]"]
---
# HU-022 — Solicitar reprogramación
## Historia de usuario
**COMO** USER **QUIERO** solicitar otra fecha/hora para una cita aprobada futura **PARA** que ADMIN decida sin perder la original.
## Contexto y descripción
Conserva profesional/especialidad; nueva franja queda `PENDING` retenida.
## Alcance
- Solicitud y retención de nueva franja.
## Fuera de alcance
- Cambiar profesional o eliminar original.
## Reglas de negocio
- Original mantiene franja hasta decisión; cambiar profesional es nueva cita.
## Dependencias y relaciones
- Épica: [[EP-005-ciclo-de-vida-de-citas]]
- Dependencias: [[HU-017-buscar-disponibilidad]], [[HU-020-consultar-mis-citas]].
- Relacionadas: [[HU-024-decidir-reprogramacion]].
## Esfuerzo
**Nivel:** Alto. **Justificación de dificultad:** dos franjas y consistencia transaccional.
## Tareas de desarrollo
- [ ] **T-01 — Documentar solicitud.** Dificultad: Alto. Definir conflicto/expiración pendiente.
- [ ] **T-02 — Implementar retención.** Dificultad: Alto. Conservar cita original.
## Criterios de aceptación
### CA-01 — Elegibilidad
**Dado** cita propia `APPROVED` futura **cuando** solicita **entonces** puede seleccionar nueva franja.
### CA-02 — Conservación
**Dado** solicitud creada **cuando** queda `PENDING` **entonces** nueva franja se retiene y original permanece.
### CA-03 — Inmutables
**Dado** cambio de profesional/especialidad **cuando** solicita **entonces** se trata como nueva cita/rechaza la reprogramación.
## Definition of Done
- [ ] CA-01 a CA-03 con evidencia.
- [ ] Contrato/UI, pruebas de doble retención/original y decisión de expiración disponibles.
- [ ] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Pendiente | — | No implementada |
| DoD | Pendiente | — | Expiración pendiente |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
## Notas y decisiones
- Continúa en [[HU-024-decidir-reprogramacion]].
