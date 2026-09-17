---
id: EP-002
tipo: epica
titulo: Perfil y catálogos
estado: Pendiente de aprobación
historias: ["[[HU-005-consultar-y-actualizar-perfil]]", "[[HU-006-gestionar-afiliacion]]", "[[HU-007-consultar-catalogos-fijos]]", "[[HU-008-administrar-eps]]", "[[HU-009-administrar-planes-eps]]", "[[HU-010-administrar-especialidades]]"]
dependencias: ["[[EP-001-acceso-e-identidad]]"]
---

# EP-002 — Perfil y catálogos

## Objetivo
Permitir perfil/afiliación USER y gestión administrativa de datos maestros normalizados.

## Valor esperado
Datos consistentes para reservar sin duplicar EPS, plan, régimen o especialidad.

## Actores
- USER
- ADMIN

## Alcance
- Perfil, afiliación, catálogos fijos, EPS, planes y especialidades/duración.

## Fuera de alcance
- Borrado físico de catálogos referenciados.

## Reglas de negocio
- Regímenes/sedes/roles/estados son fijos; EPS, planes y especialidades son configurables.

## Dependencias
- [[EP-001-acceso-e-identidad]]

## Historias de usuario
- [[HU-005-consultar-y-actualizar-perfil]]
- [[HU-006-gestionar-afiliacion]]
- [[HU-007-consultar-catalogos-fijos]]
- [[HU-008-administrar-eps]]
- [[HU-009-administrar-planes-eps]]
- [[HU-010-administrar-especialidades]]

## Criterio de completitud de la épica
- [ ] Todas las HUs obligatorias están `Completada` con evidencia.
- [ ] Las relaciones no duplican atributos transitivos.

## Riesgos e incógnitas
- Definición de Medicina General pendiente.
