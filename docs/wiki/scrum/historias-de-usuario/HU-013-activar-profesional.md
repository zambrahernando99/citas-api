---
id: HU-013
tipo: historia-de-usuario
titulo: Activar profesional
estado: Pendiente de aprobación
epica: "[[EP-003-oferta-profesional-y-disponibilidad]]"
esfuerzo: Bajo
sprint_sugerido: S3
dependencias: ["[[HU-011-crear-profesional]]"]
relacionadas: ["[[HU-017-buscar-disponibilidad]]"]
---
# HU-013 — Activar profesional
## Historia de usuario
**COMO** ADMIN **QUIERO** activar o desactivar un profesional **PARA** controlar su oferta sin borrar historial.
## Contexto y descripción
Inactivo no participa en nueva disponibilidad/reserva; impacto en compromisos previos requiere decisión.
## Alcance
- Cambio de estado operativo.
## Fuera de alcance
- Borrado de profesional.
## Reglas de negocio
- Solo ADMIN; inactivo no es elegible para nuevas franjas.
## Dependencias y relaciones
- Épica: [[EP-003-oferta-profesional-y-disponibilidad]]
- Dependencias: [[HU-011-crear-profesional]].
- Relacionadas: [[HU-017-buscar-disponibilidad]].
## Esfuerzo
**Nivel:** Bajo. **Justificación de dificultad:** regla puntual con impacto en consultas.
## Tareas de desarrollo
- [ ] **T-01 — Documentar semántica.** Dificultad: Bajo. Acordar efecto de citas vigentes.
- [ ] **T-02 — Aplicar elegibilidad.** Dificultad: Medio. Excluirlo de nuevas operaciones.
## Criterios de aceptación
### CA-01 — Cambio autorizado
**Dado** ADMIN **cuando** cambia estado **entonces** se conserva el perfil y relaciones.
### CA-02 — Exclusión
**Dado** profesional inactivo **cuando** se busca disponibilidad **entonces** no aparece para nuevas reservas.
### CA-03 — Protección
**Dado** actor no ADMIN **cuando** cambia estado **entonces** se rechaza.
## Definition of Done
- [ ] CA-01 a CA-03 con evidencia.
- [ ] Contrato, pruebas de rol/exclusión y decisión sobre compromisos vigentes disponibles.
- [ ] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Pendiente | — | No implementada |
| DoD | Pendiente | — | — |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
## Notas y decisiones
- Efecto de citas existentes es incógnita.
