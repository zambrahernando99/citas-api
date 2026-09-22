---
id: HU-005
tipo: historia-de-usuario
titulo: Consultar y actualizar perfil
estado: Aprobada
epica: "[[EP-002-perfil-y-catalogos]]"
esfuerzo: Bajo
sprint_sugerido: S3
dependencias: ["[[HU-001-registrar-user]]", "[[HU-004-autorizar-por-rol-y-propiedad]]"]
relacionadas: ["[[HU-006-gestionar-afiliacion]]"]
---
# HU-005 — Consultar y actualizar perfil
## Historia de usuario
**COMO** USER **QUIERO** consultar y actualizar mis datos permitidos **PARA** mantener mi perfil vigente.
## Contexto y descripción
La actualización preserva unicidad y no altera roles ni recursos ajenos.
## Alcance
- Lectura propia y campos permitidos por contrato.
## Fuera de alcance
- Gestión de roles o perfiles de terceros.
## Reglas de negocio
- Email/documento siguen siendo únicos; validación server-side y ownership.
## Dependencias y relaciones
- Épica: [[EP-002-perfil-y-catalogos]]
- Dependencias: [[HU-001-registrar-user]], [[HU-004-autorizar-por-rol-y-propiedad]].
- Relacionadas: [[HU-006-gestionar-afiliacion]].
## Esfuerzo
**Nivel:** Bajo. **Justificación de dificultad:** cambio localizado con validaciones y ownership.
## Tareas de desarrollo
- [ ] **T-01 — Documentar campos editables.** Dificultad: Bajo. No ampliar datos del PRD.
- [ ] **T-02 — Implementar lectura/actualización.** Dificultad: Medio. Validar propiedad y unicidad.
## Criterios de aceptación
### CA-01 — Lectura propia
**Dado** USER autenticado **cuando** consulta perfil **entonces** ve solo el suyo.
### CA-02 — Actualización válida
**Dado** datos permitidos válidos **cuando** actualiza **entonces** el perfil queda actualizado.
### CA-03 — Protección
**Dado** duplicado o recurso ajeno **cuando** actualiza **entonces** se rechaza sin cambio.
## Definition of Done
- [ ] CA-01 a CA-03 con evidencia.
- [ ] Contrato/UI REST directo y pruebas de ownership/unicidad disponibles.
- [ ] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Pendiente | — | No implementada |
| DoD | Pendiente | — | — |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
## Notas y decisiones
- Campos editables se especifican al aprobar contrato.
