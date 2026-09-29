---
id: HU-009
tipo: historia-de-usuario
titulo: Administrar planes EPS
estado: Aprobada
epica: "[[EP-002-perfil-y-catalogos]]"
esfuerzo: Bajo
sprint_sugerido: S3
dependencias: ["[[HU-008-administrar-eps]]"]
relacionadas: ["[[HU-006-gestionar-afiliacion]]"]
---
# HU-009 — Administrar planes EPS
## Historia de usuario
**COMO** ADMIN **QUIERO** gestionar planes asociados a EPS **PARA** ofrecer afiliaciones consistentes.
## Contexto y descripción
Cada plan pertenece a una EPS; un plan referenciado no se borra físicamente.
## Alcance
- CRUD administrativo y activación de planes.
## Fuera de alcance
- Plan sin EPS o borrado físico referenciado.
## Reglas de negocio
- Solo ADMIN; plan/EPS se mantienen como relación normalizada.
## Dependencias y relaciones
- Épica: [[EP-002-perfil-y-catalogos]]
- Dependencias: [[HU-008-administrar-eps]].
- Relacionadas: [[HU-006-gestionar-afiliacion]].
## Esfuerzo
**Nivel:** Bajo. **Justificación de dificultad:** catálogo acotado con FK y estado.
## Tareas de desarrollo
- [x] **T-01 — Definir contrato por EPS.** Dificultad: Bajo. Documentar opciones activas/errores.
- [x] **T-02 — Implementar integridad.** Dificultad: Medio. Validar EPS y referencias.
## Criterios de aceptación
### CA-01 — Pertenencia
**Dado** ADMIN y una EPS existente **cuando** crea un plan **entonces** queda asociado a esa EPS.
### CA-02 — No borrado
**Dado** plan referenciado **cuando** intenta eliminarlo **entonces** se conserva y puede inactivarse.
### CA-03 — Consulta útil
**Dado** EPS seleccionada **cuando** se solicitan planes **entonces** se muestran sus opciones activas.
## Definition of Done
- [x] CA-01 a CA-03 con evidencia.
- [x] Contrato, migración si aplica y pruebas de FK/rol/referencia disponibles.
- [x] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Validado backend | `AccountAndCatalogIntegrationTest#adminManagesEpsAndPlansAndUserAffiliationOnlyAcceptsActiveConsistentData`, `#updatingMissingCatalogItemReturnsNotFound` | Sin borrado físico; 404 si no existe |
| E2E | Validado | `docs/evidence/s3-s4/03-e2e-docker.md` | Gestión ADMIN de planes |
| DoD | Validado | `docs/evidence/s3-s4/01-red-green.md`, `02-hook.md`, `docs/evidence/s3-s4/03-e2e-docker.md` | Backend 40 pruebas, web 13 Vitest, E2E 47/47 |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
- 2026-09-29 — Validada en S3/S4: evidencia en docs/evidence/s3-s4/ y docs/evidence/loops/.
## Notas y decisiones
- Consumo principal: [[HU-006-gestionar-afiliacion]].
