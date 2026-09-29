---
id: HU-006
tipo: historia-de-usuario
titulo: Gestionar afiliación
estado: Aprobada
epica: "[[EP-002-perfil-y-catalogos]]"
esfuerzo: Medio
sprint_sugerido: S3
dependencias: ["[[HU-004-autorizar-por-rol-y-propiedad]]", "[[HU-007-consultar-catalogos-fijos]]", "[[HU-008-administrar-eps]]", "[[HU-009-administrar-planes-eps]]"]
relacionadas: []
---
# HU-006 — Gestionar afiliación
## Historia de usuario
**COMO** USER **QUIERO** asociar EPS, plan y régimen **PARA** conservar mi afiliación sin duplicar catálogos.
## Contexto y descripción
La afiliación referencia catálogos normalizados y pertenece al USER autenticado.
## Alcance
- Consulta/asociación de EPS, plan y régimen.
## Fuera de alcance
- Catálogos fuera de la relación de afiliación.
## Reglas de negocio
- No repetir nombres; plan debe pertenecer a EPS y referencias deben ser utilizables.
## Dependencias y relaciones
- Épica: [[EP-002-perfil-y-catalogos]]
- Dependencias: [[HU-004-autorizar-por-rol-y-propiedad]], [[HU-007-consultar-catalogos-fijos]], [[HU-008-administrar-eps]], [[HU-009-administrar-planes-eps]].
- Relacionadas: ninguna.
## Esfuerzo
**Nivel:** Medio. **Justificación de dificultad:** integra relaciones 3FN y validación cruzada.
## Tareas de desarrollo
- [x] **T-01 — Documentar relación EPS–plan.** Dificultad: Medio. Definir consulta de opciones activas.
- [x] **T-02 — Implementar afiliación.** Dificultad: Medio. Garantizar FK/ownership y pruebas.
## Criterios de aceptación
### CA-01 — Asociación válida
**Dado** catálogos válidos **cuando** USER asocia afiliación **entonces** quedan referencias consistentes.
### CA-02 — Plan compatible
**Dado** plan de otra EPS **cuando** intenta asociarlo **entonces** se rechaza.
### CA-03 — Aislamiento
**Dado** afiliación ajena **cuando** USER la modifica **entonces** se rechaza.
## Definition of Done
- [x] CA-01 a CA-03 con evidencia.
- [x] Modelo 3FN/migración Flyway si aplica y pruebas de integridad disponibles.
- [x] Contrato/UI directos coherentes; trazabilidad actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Validado backend | `AccountAndCatalogIntegrationTest#adminManagesEpsAndPlansAndUserAffiliationOnlyAcceptsActiveConsistentData` | Solo EPS/plan activos y consistentes |
| E2E | Validado | `docs/evidence/s3-s4/03-e2e-docker.md` | Afiliación |
| DoD | Validado | `docs/evidence/s3-s4/01-red-green.md`, `02-hook.md`, `docs/evidence/s3-s4/03-e2e-docker.md` | Backend 40 pruebas, web 13 Vitest, E2E 47/47 |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
- 2026-09-29 — Validada en S3/S4: evidencia en docs/evidence/s3-s4/ y docs/evidence/loops/.
## Notas y decisiones
- No se guardan nombres redundantes.
