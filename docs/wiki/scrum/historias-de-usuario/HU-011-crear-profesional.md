---
id: HU-011
tipo: historia-de-usuario
titulo: Crear profesional
estado: Pendiente de aprobación
epica: "[[EP-003-oferta-profesional-y-disponibilidad]]"
esfuerzo: Medio
sprint_sugerido: S3
dependencias: ["[[HU-004-autorizar-por-rol-y-propiedad]]"]
relacionadas: ["[[HU-012-asignar-especialidades-y-sedes]]"]
---
# HU-011 — Crear profesional
## Historia de usuario
**COMO** ADMIN **QUIERO** crear un PROFESSIONAL ficticio con código y matrícula **PARA** habilitar la oferta de agenda.
## Contexto y descripción
La creación de perfil no asigna sedes/especialidades implícitamente.
## Alcance
- Usuario profesional, código y matrícula ficticia únicos.
## Fuera de alcance
- Datos reales y asignaciones posteriores.
## Reglas de negocio
- Solo ADMIN; identidad profesional sintética.
## Dependencias y relaciones
- Épica: [[EP-003-oferta-profesional-y-disponibilidad]]
- Dependencias: [[HU-004-autorizar-por-rol-y-propiedad]].
- Relacionadas: [[HU-012-asignar-especialidades-y-sedes]].
## Esfuerzo
**Nivel:** Medio. **Justificación de dificultad:** separa identidad de usuario y profesional en 3FN.
## Tareas de desarrollo
- [ ] **T-01 — Definir datos/contrato.** Dificultad: Bajo. Identificar unicidades y validaciones.
- [ ] **T-02 — Implementar perfil.** Dificultad: Medio. Persistir relación y probar rol/duplicado.
## Criterios de aceptación
### CA-01 — Creación autorizada
**Dado** ADMIN **cuando** crea profesional válido **entonces** registra usuario PROFESSIONAL y datos ficticios.
### CA-02 — Unicidad
**Dado** código o matrícula existente **cuando** intenta repetirlo **entonces** se rechaza.
### CA-03 — Sin asignación implícita
**Dado** profesional creado **cuando** se consulta **entonces** no aparece habilitado en sede/especialidad no asignada.
## Definition of Done
- [ ] CA-01 a CA-03 con evidencia.
- [ ] Contrato, migración si aplica, datos sintéticos y pruebas de rol/unicidad disponibles.
- [ ] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Pendiente | — | No implementada |
| DoD | Pendiente | — | — |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
## Notas y decisiones
- Asignaciones se realizan en [[HU-012-asignar-especialidades-y-sedes]].
