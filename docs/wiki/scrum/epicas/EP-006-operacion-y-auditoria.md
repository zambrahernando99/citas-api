---
id: EP-006
tipo: epica
titulo: Operación y auditoría
estado: Pendiente de aprobación
historias: ["[[HU-025-consultar-agenda-profesional]]", "[[HU-026-cerrar-atencion]]", "[[HU-027-consultar-bandeja-administrativa]]", "[[HU-028-auditar-cambios-de-estado]]"]
dependencias: ["[[EP-001-acceso-e-identidad]]"]
---

# EP-006 — Operación y auditoría

## Objetivo
Permitir la operación autorizada de agendas y pendientes, conservando un historial de estados no editable.

## Valor esperado
Profesionales y ADMIN trabajan con información pertinente, y toda transición queda trazable.

## Actores
- PROFESSIONAL
- ADMIN
- USER

## Alcance
- Agenda aprobada, cierre de atención, bandeja pendiente e historial de estados.

## Fuera de alcance
- CRUD normal de auditoría o consulta masiva de datos de pacientes.

## Reglas de negocio
- Auditoría registra cita, estado, actor, fuente, fecha/hora y motivo opcional; no se modifica como CRUD.

## Dependencias
- [[EP-001-acceso-e-identidad]]

## Historias de usuario
- [[HU-025-consultar-agenda-profesional]]
- [[HU-026-cerrar-atencion]]
- [[HU-027-consultar-bandeja-administrativa]]
- [[HU-028-auditar-cambios-de-estado]]

## Criterio de completitud de la épica
- [ ] Todas las HUs obligatorias están `Completada` con evidencia.
- [ ] Ningún cambio de estado del alcance queda sin historial.

## Riesgos e incógnitas
- Elegibilidad de cierre y conjunto completo de estados pendientes.
