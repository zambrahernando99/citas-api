---
id: EP-003
tipo: epica
titulo: Oferta profesional y disponibilidad
estado: Pendiente de aprobación
historias: ["[[HU-011-crear-profesional]]", "[[HU-012-asignar-especialidades-y-sedes]]", "[[HU-013-activar-profesional]]", "[[HU-014-crear-bloques-disponibilidad]]", "[[HU-015-modificar-bloques-futuros]]", "[[HU-016-consultar-calendario-profesional]]", "[[HU-017-buscar-disponibilidad]]"]
dependencias: ["[[EP-001-acceso-e-identidad]]", "[[EP-002-perfil-y-catalogos]]"]
---

# EP-003 — Oferta profesional y disponibilidad

## Objetivo
Habilitar profesionales ficticios y bloques futuros para ofrecer franjas reservables.

## Valor esperado
USER encuentra disponibilidad válida por sede, especialidad, profesional y fecha.

## Actores
- ADMIN
- PROFESSIONAL
- USER

## Alcance
- Profesional, asignaciones, activación, bloques, calendario y búsqueda.

## Fuera de alcance
- Agenda clínica o reserva de citas.

## Reglas de negocio
- No pasado/solapamiento; slots de 30 min; sede/especialidad asignadas y activas.

## Dependencias
- [[EP-001-acceso-e-identidad]]
- [[EP-002-perfil-y-catalogos]]

## Historias de usuario
- [[HU-011-crear-profesional]]
- [[HU-012-asignar-especialidades-y-sedes]]
- [[HU-013-activar-profesional]]
- [[HU-014-crear-bloques-disponibilidad]]
- [[HU-015-modificar-bloques-futuros]]
- [[HU-016-consultar-calendario-profesional]]
- [[HU-017-buscar-disponibilidad]]

## Criterio de completitud de la épica
- [ ] Todas las HUs obligatorias están `Completada` con evidencia.
- [ ] Búsqueda no propone franjas incompatibles, ocupadas o retenidas.

## Riesgos e incógnitas
- Mecanismo exacto de concurrencia se decide en diseño aprobado.
