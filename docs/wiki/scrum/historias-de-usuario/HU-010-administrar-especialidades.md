---
id: HU-010
tipo: historia-de-usuario
titulo: Administrar especialidades
estado: Pendiente de aprobación
epica: "[[EP-002-perfil-y-catalogos]]"
esfuerzo: Medio
sprint_sugerido: S3
dependencias: ["[[HU-004-autorizar-por-rol-y-propiedad]]"]
relacionadas: ["[[HU-012-asignar-especialidades-y-sedes]]", "[[HU-017-buscar-disponibilidad]]"]
---
# HU-010 — Administrar especialidades
## Historia de usuario
**COMO** ADMIN **QUIERO** gestionar especialidades y duración **PARA** que la agenda respete 30 o 60 minutos.
## Contexto y descripción
Especialidades configurables activas, con duración exclusiva 30/60; profesional no la altera.
## Alcance
- CRUD, activación y duración.
## Fuera de alcance
- Borrado físico referenciado o duración distinta.
## Reglas de negocio
- Especialidad activa/asociada para reservar; 60 requiere dos slots consecutivos.
## Dependencias y relaciones
- Épica: [[EP-002-perfil-y-catalogos]]
- Dependencias: [[HU-004-autorizar-por-rol-y-propiedad]].
- Relacionadas: [[HU-012-asignar-especialidades-y-sedes]], [[HU-017-buscar-disponibilidad]].
## Esfuerzo
**Nivel:** Medio. **Justificación de dificultad:** regla de duración impacta disponibilidad y reserva.
## Tareas de desarrollo
- [ ] **T-01 — Definir catálogo/duración.** Dificultad: Medio. Restringir 30/60 y actividad.
- [ ] **T-02 — Propagar consulta.** Dificultad: Medio. Exponer para agenda sin duplicar valor.
## Criterios de aceptación
### CA-01 — Duración válida
**Dado** ADMIN **cuando** guarda especialidad **entonces** solo acepta 30 o 60 minutos.
### CA-02 — Referencia protegida
**Dado** especialidad en uso **cuando** intenta eliminarla **entonces** no se borra físicamente.
### CA-03 — No sobrescritura
**Dado** profesional o reserva **cuando** usa especialidad **entonces** aplica su duración configurada.
## Definition of Done
- [ ] CA-01 a CA-03 con evidencia.
- [ ] Contrato, migración si aplica y pruebas de duración/rol/referencia disponibles.
- [ ] Medicina General decidida o marcada bloqueante para HU-018.
- [ ] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Pendiente | — | No implementada |
| DoD | Pendiente | — | — |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
## Notas y decisiones
- Medicina General requiere revisión explícita.
