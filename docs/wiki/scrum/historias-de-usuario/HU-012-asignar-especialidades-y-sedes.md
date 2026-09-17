---
id: HU-012
tipo: historia-de-usuario
titulo: Asignar especialidades y sedes
estado: Pendiente de aprobación
epica: "[[EP-003-oferta-profesional-y-disponibilidad]]"
esfuerzo: Medio
sprint_sugerido: S3
dependencias: ["[[HU-007-consultar-catalogos-fijos]]", "[[HU-010-administrar-especialidades]]", "[[HU-011-crear-profesional]]"]
relacionadas: ["[[HU-014-crear-bloques-disponibilidad]]"]
---
# HU-012 — Asignar especialidades y sedes
## Historia de usuario
**COMO** ADMIN **QUIERO** asignar especialidades y una o ambas sedes a un profesional **PARA** limitar su oferta correctamente.
## Contexto y descripción
Permite varias especialidades y una primaria; relaciones N:M normalizadas.
## Alcance
- Asignaciones profesionales y especialidad primaria.
## Fuera de alcance
- Crear especialidad/sede desde esta HU.
## Reglas de negocio
- Profesional solo publica/reserva en sede y especialidad asignadas.
## Dependencias y relaciones
- Épica: [[EP-003-oferta-profesional-y-disponibilidad]]
- Dependencias: [[HU-007-consultar-catalogos-fijos]], [[HU-010-administrar-especialidades]], [[HU-011-crear-profesional]].
- Relacionadas: [[HU-014-crear-bloques-disponibilidad]].
## Esfuerzo
**Nivel:** Medio. **Justificación de dificultad:** N:M, primaria e impacto en elegibilidad.
## Tareas de desarrollo
- [ ] **T-01 — Modelar relaciones.** Dificultad: Medio. Definir primaria y restricciones.
- [ ] **T-02 — Exponer administración.** Dificultad: Medio. Validar activos y pruebas.
## Criterios de aceptación
### CA-01 — Especialidades
**Dado** ADMIN **cuando** asigna especialidades activas **entonces** puede marcar una primaria.
### CA-02 — Sedes
**Dado** ADMIN **cuando** asigna sedes fijas **entonces** el profesional queda habilitado en una o ambas.
### CA-03 — Elegibilidad
**Dado** combinación no asignada **cuando** se publica o reserva **entonces** se rechaza/excluye.
## Definition of Done
- [ ] CA-01 a CA-03 con evidencia.
- [ ] Migración si aplica, contrato y pruebas N:M/primaria/actividad disponibles.
- [ ] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Pendiente | — | No implementada |
| DoD | Pendiente | — | — |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
## Notas y decisiones
- No se guardan listas en columnas.
