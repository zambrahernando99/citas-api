---
id: HU-007
tipo: historia-de-usuario
titulo: Consultar catálogos fijos
estado: Pendiente de aprobación
epica: "[[EP-002-perfil-y-catalogos]]"
esfuerzo: Medio
sprint_sugerido: S3
dependencias: ["[[HU-004-autorizar-por-rol-y-propiedad]]"]
relacionadas: ["[[HU-006-gestionar-afiliacion]]", "[[HU-012-asignar-especialidades-y-sedes]]"]
---
# HU-007 — Consultar catálogos fijos
## Historia de usuario
**COMO** participante autorizado **QUIERO** consultar catálogos fijos **PARA** seleccionar valores válidos en las capacidades que los requieren.
## Contexto y descripción
Roles, estados de cita/reprogramación, regímenes y sedes son de solo lectura y precargados.
## Alcance
- Seed y consulta de catálogos fijos.
## Fuera de alcance
- CRUD de catálogos fijos.
## Reglas de negocio
- Incluye HIC e ICV como sedes fijas; no se borran/modifican vía administración.
## Dependencias y relaciones
- Épica: [[EP-002-perfil-y-catalogos]]
- Dependencias: [[HU-004-autorizar-por-rol-y-propiedad]].
- Relacionadas: [[HU-006-gestionar-afiliacion]], [[HU-012-asignar-especialidades-y-sedes]].
## Esfuerzo
**Nivel:** Medio. **Justificación de dificultad:** requiere seed, modelo normalizado y contrato de lectura.
## Tareas de desarrollo
- [ ] **T-01 — Documentar valores y contrato.** Dificultad: Bajo. Identificar catálogo/consumidor.
- [ ] **T-02 — Preparar seed y lectura.** Dificultad: Medio. Usar migración Flyway y proteger edición.
## Criterios de aceptación
### CA-01 — Disponibilidad fija
**Dado** una consulta autorizada **cuando** solicita un catálogo fijo **entonces** recibe sus valores precargados.
### CA-02 — Solo lectura
**Dado** un intento de administrar un catálogo fijo **cuando** se ejecuta **entonces** no se permite.
### CA-03 — Sedes
**Dado** catálogo de sedes **cuando** se consulta **entonces** incluye HIC e ICV del PRD.
## Definition of Done
- [ ] CA-01 a CA-03 con evidencia.
- [ ] Migración/seed Flyway coherente y pruebas de lectura/no edición disponibles.
- [ ] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Pendiente | — | No implementada |
| DoD | Pendiente | — | — |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
## Notas y decisiones
- Catálogo exacto de transiciones de estado continúa pendiente.
