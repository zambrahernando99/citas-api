---
id: EP-001
tipo: epica
titulo: Acceso e identidad
estado: Aprobada
historias: ["[[HU-001-registrar-user]]", "[[HU-002-gestionar-sesion-jwt]]", "[[HU-003-recuperar-contrasena]]", "[[HU-004-autorizar-por-rol-y-propiedad]]"]
dependencias: []
---

# EP-001 — Acceso e identidad

## Objetivo
Permitir registro, acceso protegido, recuperación y permisos verificables.

## Valor esperado
Habilita las capacidades posteriores sin exponer credenciales, tokens o recursos ajenos.

## Actores
- Visitante
- USER
- PROFESSIONAL
- ADMIN

## Alcance
- Registro USER, sesión JWT access/refresh, recuperación y autorización por rol/propiedad.

## Fuera de alcance
- Identidad externa, SMTP obligatorio y datos reales.

## Reglas de negocio
- Email/documento únicos; hash adaptativo; secretos por environment; access y refresh separados.

## Dependencias
- Ninguna.

## Historias de usuario
- [[HU-001-registrar-user]]
- [[HU-002-gestionar-sesion-jwt]]
- [[HU-003-recuperar-contrasena]]
- [[HU-004-autorizar-por-rol-y-propiedad]]

## Criterio de completitud de la épica
- [ ] Todas las HUs obligatorias están `Completada` con evidencia.
- [ ] No quedan permisos o secretos sin tratamiento dentro del alcance.

## Riesgos e incógnitas
- Roles combinados y entrega segura de recuperación pendientes.
