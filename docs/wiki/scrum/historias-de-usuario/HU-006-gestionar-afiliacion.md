---
id: HU-006
tipo: historia-de-usuario
titulo: Gestionar afiliación
estado: Pendiente de aprobación
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
- [ ] **T-01 — Documentar relación EPS–plan.** Dificultad: Medio. Definir consulta de opciones activas.
- [ ] **T-02 — Implementar afiliación.** Dificultad: Medio. Garantizar FK/ownership y pruebas.
## Criterios de aceptación
### CA-01 — Asociación válida
**Dado** catálogos válidos **cuando** USER asocia afiliación **entonces** quedan referencias consistentes.
### CA-02 — Plan compatible
**Dado** plan de otra EPS **cuando** intenta asociarlo **entonces** se rechaza.
### CA-03 — Aislamiento
**Dado** afiliación ajena **cuando** USER la modifica **entonces** se rechaza.
## Definition of Done
- [ ] CA-01 a CA-03 con evidencia.
- [ ] Modelo 3FN/migración Flyway si aplica y pruebas de integridad disponibles.
- [ ] Contrato/UI directos coherentes; trazabilidad actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Pendiente | — | No implementada |
| DoD | Pendiente | — | — |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
## Notas y decisiones
- No se guardan nombres redundantes.
