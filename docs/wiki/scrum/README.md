# Mapa Scrum / Spec-Driven Development — Citas ficticias

## Estado del mapa

Las HU-001 a HU-028 están en estado `Aprobada` por autorización explícita del usuario el 2026-09-22. La aprobación habilita su planificación e implementación, pero no declara ninguna HU como desarrollada, validada o completada. Este mapa usa exclusivamente `PRD.md`, `RESTRICCIONES_TECNICAS.md` y `database/REQUISITOS_NORMALIZACION_3FN.md`; no prescribe código, endpoints, tablas ni componentes no definidos.

## Stack y límites detectados

- Backend requerido: Java 21, Spring Boot 3.5.x, Maven, arquitectura hexagonal, Spring Data JPA, MySQL 8.4, Flyway y Spring Security con JWT access/refresh.
- Frontend requerido: TypeScript, Node.js 24, React o Angular por selección del estudiante; REST directo a backend, sin Express/BFF.
- No existe bootstrap de aplicación. La elección React/Angular permanece pendiente y no cambia las HUs.

## Épicas propuestas

- [[EP-001-acceso-e-identidad]]
- [[EP-002-perfil-y-catalogos]]
- [[EP-003-oferta-profesional-y-disponibilidad]]
- [[EP-004-reserva-y-consulta-de-citas]]
- [[EP-005-ciclo-de-vida-de-citas]]
- [[EP-006-operacion-y-auditoria]]

## Incrementos funcionales sugeridos

| Incremento | Resultado comprobable | HUs propuestas |
| --- | --- | --- |
| S2 — Acceso y base | Registro, sesión y permisos por rol. | [[HU-001-registrar-user]], [[HU-002-gestionar-sesion-jwt]], [[HU-003-recuperar-contrasena]], [[HU-004-autorizar-por-rol-y-propiedad]] |
| S3 — Perfil y oferta | Perfil/afiliación, catálogos y profesionales habilitados. | [[HU-005-consultar-y-actualizar-perfil]], [[HU-006-gestionar-afiliacion]], [[HU-007-consultar-catalogos-fijos]], [[HU-008-administrar-eps]], [[HU-009-administrar-planes-eps]], [[HU-010-administrar-especialidades]], [[HU-011-crear-profesional]], [[HU-012-asignar-especialidades-y-sedes]], [[HU-013-activar-profesional]] |
| S4 — Disponibilidad y reserva | Bloques, búsqueda, reserva y consulta de citas trazables. | [[HU-014-crear-bloques-disponibilidad]], [[HU-015-modificar-bloques-futuros]], [[HU-016-consultar-calendario-profesional]], [[HU-017-buscar-disponibilidad]], [[HU-018-reservar-cita-general]], [[HU-019-solicitar-cita-especializada]], [[HU-020-consultar-mis-citas]], [[HU-028-auditar-cambios-de-estado]] |
| S5 — Ciclo de vida | Cancelaciones y decisiones preservan reservas y cita original. | [[HU-021-cancelar-cita]], [[HU-022-solicitar-reprogramacion]], [[HU-023-decidir-cita-especializada]], [[HU-024-decidir-reprogramacion]] |
| S6 — Operación | Agenda profesional, cierre y bandeja administrativa. | [[HU-025-consultar-agenda-profesional]], [[HU-026-cerrar-atencion]], [[HU-027-consultar-bandeja-administrativa]] |

La selección para S2, S3 y S4 exige aprobación explícita de cada HU. Los incrementos expresan orden/dependencias, nunca duración, capacidad o estimación.

## Incógnitas para revisión

1. Expiración de retenciones especializadas y de reprogramación pendiente.
2. Catálogo total de estados, terminalidad y transiciones fuera de los estados expresos.
3. Naturaleza fija o configurable de Medicina General.
4. Coexistencia o exclusividad de roles por cuenta.
5. Regla de “pasada/aplicable” y corrección de cierre clínico.
6. Entrega segura del token de recuperación en desarrollo.
