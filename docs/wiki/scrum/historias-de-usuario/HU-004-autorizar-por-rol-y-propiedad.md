---
id: HU-004
tipo: historia-de-usuario
titulo: Autorizar por rol y propiedad
estado: Aprobada
epica: "[[EP-001-acceso-e-identidad]]"
esfuerzo: Alto
sprint_sugerido: S2
dependencias: ["[[HU-002-gestionar-sesion-jwt]]"]
relacionadas: ["[[HU-005-consultar-y-actualizar-perfil]]", "[[HU-028-auditar-cambios-de-estado]]"]
---
# HU-004 — Autorizar por rol y propiedad
## Historia de usuario
**COMO** participante autenticado **QUIERO** que mis permisos se limiten a mi rol y recursos propios **PARA** proteger la operación.
## Contexto y descripción
USER opera sus recursos; PROFESSIONAL los propios; ADMIN catálogos, profesionales y pendientes.
## Alcance
- Matriz rol/acción/recurso y ownership.
## Fuera de alcance
- Resolver por inferencia coexistencia de roles.
## Reglas de negocio
- Acceso sin autenticación, rol o propiedad se rechaza sin revelar recursos.
## Dependencias y relaciones
- Épica: [[EP-001-acceso-e-identidad]]
- Dependencias: [[HU-002-gestionar-sesion-jwt]].
- Relacionadas: [[HU-005-consultar-y-actualizar-perfil]], [[HU-028-auditar-cambios-de-estado]].
## Esfuerzo
**Nivel:** Alto. **Justificación de dificultad:** condición transversal a todas las APIs y vistas.
## Tareas de desarrollo
- [x] **T-01 — Documentar matriz de permisos.** Dificultad: Medio. Mapear roles contra HUs aprobadas.
- [x] **T-02 — Aplicar autorización.** Dificultad: Alto. Validar rol/ownership y respuestas negativas.
## Criterios de aceptación
### CA-01 — USER aislado
**Dado** un USER autenticado **cuando** accede a recurso ajeno **entonces** se rechaza sin exponerlo.
### CA-02 — Rol requerido
**Dado** una acción administrativa/profesional **cuando** la ejecuta un rol distinto **entonces** se rechaza.
### CA-03 — Acceso propio
**Dado** actor con rol y propiedad válidos **cuando** ejecuta acción permitida **entonces** puede continuar.
## Definition of Done
- [x] CA-01 a CA-03 validados con evidencia.
- [x] Matriz enlazada desde contratos afectados y pruebas negativas disponibles.
- [x] Política de roles combinados decidida o impacto bloqueado explícitamente.
- [x] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Validado backend | `AppointmentLifecycleIntegrationTest#rolesAreEnforcedOnProtectedRoutes`, `#onlyOwnerCancelsFutureAppointmentAndSlotsAreReleasedWithAudit`, `#statusHistoryRecordsEveryTransitionAndIsReadableOnlyByAuthorizedActors`; `ProfessionalOfferIntegrationTest`; `ErrorDispatchIntegrationTest` | Mutaciones no ADMIN rechazadas; 401 solo sin token; `/error` permitido |
| UI | Validado | `ReconciliationScreens.test.tsx` | Menú por rol desde `src/navigation.ts` |
| E2E | Validado | `docs/evidence/s3-s4/03-e2e-docker.md` | 403/401 observados |
| DoD | Validado | `docs/evidence/s3-s4/01-red-green.md`, `02-hook.md`, `docs/evidence/s3-s4/03-e2e-docker.md` | Backend 40 pruebas, web 13 Vitest, E2E 47/47 |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
- 2026-09-29 — Validada en S3/S4: evidencia en docs/evidence/s3-s4/ y docs/evidence/loops/.
## Notas y decisiones
- Requisito transversal para HUs protegidas.
