---
id: HU-013
tipo: historia-de-usuario
titulo: Activar profesional
estado: Aprobada
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
- [x] **T-01 — Documentar semántica.** Dificultad: Bajo. Acordar efecto de citas vigentes.
- [x] **T-02 — Aplicar elegibilidad.** Dificultad: Medio. Excluirlo de nuevas operaciones.
## Criterios de aceptación
### CA-01 — Cambio autorizado
**Dado** ADMIN **cuando** cambia estado **entonces** se conserva el perfil y relaciones.
### CA-02 — Exclusión
**Dado** profesional inactivo **cuando** se busca disponibilidad **entonces** no aparece para nuevas reservas.
### CA-03 — Protección
**Dado** actor no ADMIN **cuando** cambia estado **entonces** se rechaza.
## Definition of Done
- [x] CA-01 a CA-03 con evidencia.
- [x] Contrato, pruebas de rol/exclusión y decisión sobre compromisos vigentes disponibles.
- [x] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Validado backend | `ProfessionalOfferIntegrationTest` (6 pruebas) | Activación/inactivación ADMIN |
| E2E | Validado | `docs/evidence/s3-s4/03-e2e-docker.md` | Profesional activo con oferta |
| DoD | Validado | `docs/evidence/s3-s4/01-red-green.md`, `02-hook.md`, `docs/evidence/s3-s4/03-e2e-docker.md` | Backend 40 pruebas, web 13 Vitest, E2E 47/47 |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
- 2026-09-29 — Validada en S3/S4: evidencia en docs/evidence/s3-s4/ y docs/evidence/loops/.
## Notas y decisiones
- Efecto de citas existentes es incógnita.
