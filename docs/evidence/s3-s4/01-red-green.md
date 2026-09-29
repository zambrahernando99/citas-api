# S3/S4 — Evidencia Red → Green (backend)

Fecha: 2026-09-29 · Rama: `develop` · Base: `da7866b` · Ejecución: `docker compose exec citas-api-dev mvn test` (Java 21, Maven 3.9, H2 modo MySQL)

## 1. RED — pruebas escritas antes de la implementación

Se creó `AppointmentLifecycleIntegrationTest` (14 pruebas: slots 30/60, doble reserva, retención REQUESTED, bloques comprometidos, cancelación, reprogramación, cierre, autorización por rol, historial HU-028, bandeja HU-027, IDs de referencia).

Resultado inicial: **14 ejecutadas, 3 fallas, 1 error**:

```text
[ERROR] Tests run: 14, Failures: 3, Errors: 1, Skipped: 0, Time elapsed: 21.96 s <<< FAILURE! -- in co.academy.citas.adapter.in.web.AppointmentLifecycleIntegrationTest
[ERROR] co.academy.citas.adapter.in.web.AppointmentLifecycleIntegrationTest.myAppointmentsOnlyReturnsOwnAppointmentsWithReferenceIds -- Time elapsed: 0.097 s <<< FAILURE!
java.lang.AssertionError: No value at JSON path "$[0].professionalId"
[ERROR] co.academy.citas.adapter.in.web.AppointmentLifecycleIntegrationTest.adminInboxCombinesRequestedAndPendingReschedulesWithFiltersAndNoSideEffects -- Time elapsed: 0.106 s <<< FAILURE!
java.lang.AssertionError: Status expected:<200> but was:<404>
	at org.springframework.test.util.AssertionErrors.fail(AssertionErrors.java:61)
	at org.springframework.test.util.AssertionErrors.assertEquals(AssertionErrors.java:128)
[ERROR] co.academy.citas.adapter.in.web.AppointmentLifecycleIntegrationTest.statusHistoryRecordsEveryTransitionAndIsReadableOnlyByAuthorizedActors -- Time elapsed: 0.056 s <<< FAILURE!
java.lang.AssertionError: Status expected:<200> but was:<404>
	at org.springframework.test.util.AssertionErrors.fail(AssertionErrors.java:61)
	at org.springframework.test.util.AssertionErrors.assertEquals(AssertionErrors.java:128)
[ERROR] co.academy.citas.adapter.in.web.AppointmentLifecycleIntegrationTest.rescheduleKeepsOriginalWhilePendingRejectReleasesNewAndApproveSwaps -- Time elapsed: 0.091 s <<< ERROR!
```

| Prueba roja | Causa | Tipo |
|---|---|---|
| `statusHistoryRecordsEveryTransition…` | No existía `GET /appointments/{id}/history` (404) | Funcionalidad faltante HU-028 |
| `adminInboxCombinesRequestedAndPending…` | No existía `GET /admin/inbox` (404) | Funcionalidad faltante HU-027 |
| `myAppointmentsOnlyReturnsOwn…WithReferenceIds` | `AppointmentResponse` sin `professionalId/specialtyId/locationId` | Brecha de contrato (reprogramación web resolvía IDs por nombre) |
| `rescheduleKeepsOriginalWhilePending…` | `getLong()` sobre `appointment_reschedule.id BINARY(16)` → HTTP 500 en vez de 409; aprobación sin auditoría | **Defecto real** (corregido en LOOP_01) |

## 2. Hook bloquea el commit en rojo

`git commit` con la prueba roja staged → `.githooks/pre-commit` → `scripts/verify-s3.ps1` → `mvn test`:

```text
[ERROR] Tests run: 32, Failures: 3, Errors: 1, Skipped: 0
[INFO] BUILD FAILURE
exit=1  (commit rechazado; HEAD permaneció en da7866b)
```

## 3. GREEN

| Paso | Cambio | Resultado |
|---|---|---|
| LOOP_01 (Builder/Verifier) | `getBytes` en verificación de PENDING; historial ADMIN al aprobar reprogramación | Prueba de reprogramación en verde |
| Implementación HU-028 | `GET /api/v1/appointments/{id}/history` (dueño, profesional asignado o ADMIN; otro actor → 404; PUT/DELETE → 405; no expone `changedBy`) | Verde |
| Implementación HU-027 | `GET /api/v1/admin/inbox?type&locationId&professionalId&specialtyId&from&to` | Verde |
| Contrato aditivo | `professionalId`, `specialtyId`, `locationId` en `AppointmentResponse` | Verde |

Suite completa tras GREEN: **32 ejecutadas, 0 fallas**.

## 4. Segunda ronda Red → Green (HU-003, HU-005..HU-010)

`AccountAndCatalogIntegrationTest` (7 pruebas) destapó:

| Hallazgo | Corrección |
|---|---|
| `PUT /admin/eps/{id}` o `/admin/specialties/{id}` inexistente → 500 (`NoSuchElementException`) | Manejador → 404 `not_found` |
| Regímenes sin seed y con CRUD admin, contra PRD RF-05 / HU-007 CA-02 | Decisión del usuario: cumplir PRD. `V7__seed_fixed_regimes.sql` (seed idempotente) y retiro de POST/PUT/PATCH/DELETE `/admin/regimes` |
| Mensajes de validación de catálogo devolvían siempre "Professional specialties or locations are invalid" | `detail` usa el mensaje real de validación (sin datos sensibles) |

Suite completa final: **39 ejecutadas, 0 fallas, BUILD SUCCESS**.
