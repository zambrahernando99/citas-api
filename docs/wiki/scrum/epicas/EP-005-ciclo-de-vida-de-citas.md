---
id: EP-005
tipo: epica
titulo: Ciclo de vida de citas
estado: Pendiente de aprobación
historias: ["[[HU-021-cancelar-cita]]", "[[HU-022-solicitar-reprogramacion]]", "[[HU-023-decidir-cita-especializada]]", "[[HU-024-decidir-reprogramacion]]"]
dependencias: ["[[EP-004-reserva-y-consulta-de-citas]]"]
---

# EP-005 — Ciclo de vida de citas

## Objetivo
Resolver cancelaciones, decisiones y reprogramaciones preservando la disponibilidad y la cita original.

## Valor esperado
Evita pérdida de franjas, estados inconsistentes y decisiones sin trazabilidad.

## Actores
- USER
- ADMIN

## Alcance
- Cancelación, solicitudes de reprogramación y decisiones ADMIN.

## Fuera de alcance
- Reactivar directamente una cita cancelada o cambiar profesional al reprogramar.

## Reglas de negocio
- Rechazo requiere motivo; cancelación/rechazo liberan; original se conserva hasta aprobar reprogramación.

## Dependencias
- [[EP-004-reserva-y-consulta-de-citas]]

## Historias de usuario
- [[HU-021-cancelar-cita]]
- [[HU-022-solicitar-reprogramacion]]
- [[HU-023-decidir-cita-especializada]]
- [[HU-024-decidir-reprogramacion]]

## Criterio de completitud de la épica
- [ ] Todas las HUs obligatorias están `Completada` con evidencia.
- [ ] Cada decisión conserva/libera las franjas que el PRD exige.

## Riesgos e incógnitas
- Terminalidad/transiciones y expiración de retenciones pendientes.
