---
id: HU-014
tipo: historia-de-usuario
titulo: Crear bloques disponibilidad
estado: Aprobada
epica: "[[EP-003-oferta-profesional-y-disponibilidad]]"
esfuerzo: Alto
sprint_sugerido: S4
dependencias: ["[[HU-004-autorizar-por-rol-y-propiedad]]", "[[HU-012-asignar-especialidades-y-sedes]]", "[[HU-013-activar-profesional]]"]
relacionadas: ["[[HU-015-modificar-bloques-futuros]]", "[[HU-017-buscar-disponibilidad]]"]
---
# HU-014 — Crear bloques disponibilidad
## Historia de usuario
**COMO** PROFESSIONAL **QUIERO** crear bloques futuros por día y sede **PARA** publicar disponibilidad.
## Contexto y descripción
Se permiten múltiples bloques/día; se discretizan en slots de 30 minutos.
## Alcance
- Crear bloques propios por sede asignada.
## Fuera de alcance
- Bloques pasados, ajenos o solapados.
## Reglas de negocio
- No pasado/solapamiento; profesional habilitado en sede.
## Dependencias y relaciones
- Épica: [[EP-003-oferta-profesional-y-disponibilidad]]
- Dependencias: [[HU-004-autorizar-por-rol-y-propiedad]], [[HU-012-asignar-especialidades-y-sedes]], [[HU-013-activar-profesional]].
- Relacionadas: [[HU-015-modificar-bloques-futuros]], [[HU-017-buscar-disponibilidad]].
## Esfuerzo
**Nivel:** Alto. **Justificación de dificultad:** reglas temporales, solapamiento y consistencia.
## Tareas de desarrollo
- [x] **T-01 — Diseñar bloque/slots.** Dificultad: Alto. Definir validación e índices.
- [x] **T-02 — Implementar creación.** Dificultad: Alto. Proteger solapamiento y ownership.
## Criterios de aceptación
### CA-01 — Bloque válido
**Dado** profesional activo y sede asignada **cuando** crea bloque futuro **entonces** se publica en slots de 30 minutos.
### CA-02 — Rechazo temporal
**Dado** fecha pasada o solapamiento **cuando** crea bloque **entonces** se rechaza.
### CA-03 — Sede/propiedad
**Dado** sede no asignada o profesional ajeno **cuando** crea bloque **entonces** se rechaza.
## Definition of Done
- [x] CA-01 a CA-03 con evidencia.
- [x] Migración Flyway si aplica, contrato y pruebas de solapamiento/ownership disponibles.
- [x] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Validado backend | `AppointmentLifecycleIntegrationTest#professionalCannotEditOrDeleteCommittedBlockOrOverlapBlocks`; `AppointmentFlowIntegrationTest#professionalCreatesBlocksAndGeneralAppointmentIsApprovedWithoutDoubleBooking` | Sin solapamiento; USER 403 |
| E2E | Validado | `docs/evidence/s3-s4/03-e2e-docker.md` | Bloque y solapamiento |
| DoD | Validado | `docs/evidence/s3-s4/01-red-green.md`, `02-hook.md`, `docs/evidence/s3-s4/03-e2e-docker.md` | Backend 40 pruebas, web 13 Vitest, E2E 47/47 |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
- 2026-09-29 — Validada en S3/S4: evidencia en docs/evidence/s3-s4/ y docs/evidence/loops/.
## Notas y decisiones
- Concurrencia se prueba al implementar.
