---
id: HU-008
tipo: historia-de-usuario
titulo: Administrar EPS
estado: Aprobada
epica: "[[EP-002-perfil-y-catalogos]]"
esfuerzo: Bajo
sprint_sugerido: S3
dependencias: ["[[HU-004-autorizar-por-rol-y-propiedad]]"]
relacionadas: ["[[HU-009-administrar-planes-eps]]"]
---
# HU-008 — Administrar EPS
## Historia de usuario
**COMO** ADMIN **QUIERO** gestionar EPS activas/inactivas **PARA** mantener el catálogo de afiliación.
## Contexto y descripción
CRUD administrativo sin borrado físico de EPS referenciadas.
## Alcance
- Crear, consultar, actualizar y activar/desactivar EPS.
## Fuera de alcance
- Eliminar físicamente EPS usada.
## Reglas de negocio
- Solo ADMIN; una EPS referenciada se conserva.
## Dependencias y relaciones
- Épica: [[EP-002-perfil-y-catalogos]]
- Dependencias: [[HU-004-autorizar-por-rol-y-propiedad]].
- Relacionadas: [[HU-009-administrar-planes-eps]].
## Esfuerzo
**Nivel:** Bajo. **Justificación de dificultad:** catálogo localizado con integridad referencial.
## Tareas de desarrollo
- [ ] **T-01 — Documentar CRUD y estado.** Dificultad: Bajo. Definir errores/consulta activa.
- [ ] **T-02 — Implementar integridad.** Dificultad: Medio. Evitar borrado físico referenciado.
## Criterios de aceptación
### CA-01 — Administración autorizada
**Dado** ADMIN **cuando** gestiona EPS **entonces** puede crear/actualizar/activar/desactivar.
### CA-02 — Protección de referencia
**Dado** EPS referenciada **cuando** intenta eliminarla **entonces** no se borra físicamente.
### CA-03 — Consumo
**Dado** afiliación **cuando** consulta opciones **entonces** distingue EPS activas.
## Definition of Done
- [ ] CA-01 a CA-03 con evidencia.
- [ ] Contrato, pruebas de rol/referencia y migración si aplica disponibles.
- [ ] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Pendiente | — | No implementada |
| DoD | Pendiente | — | — |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
## Notas y decisiones
- Datos sintéticos.
