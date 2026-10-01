# S5 — Red → Green del contrato de recordatorios (WF-001)

Fecha: 2026-10-01. Rama: `develop`. Entorno: contenedor `fcv-citas-api-dev` (Maven 3.9, Java 21), H2 en modo MySQL con Flyway.

## Red

La prueba `AutomationReminderIntegrationTest` se escribió antes que el código, sin referenciar clases nuevas, para que compilara y fallara por comportamiento:

```text
mvn -q test -Dtest=AutomationReminderIntegrationTest
[ERROR]   AutomationReminderIntegrationTest.setup:54 » BadSqlGrammar ... [delete from appointment_reminder_delivery]
[ERROR] Tests run: 10, Failures: 0, Errors: 10, Skipped: 0
```

Causa: no existía la tabla `appointment_reminder_delivery` ni los endpoints `/api/v1/automation/**`.

## Green

Implementación: V8, `AppointmentReminderService`, `AppointmentReminderPersistenceAdapter`, `AutomationReminderController`, `AutomationApiKeyFilter` y la regla `hasRole("AUTOMATION")`.

```text
mvn -q test
AutomationReminderIntegrationTest   10/10
AppointmentReminderServiceTest       4/4
Suite completa                      54 pruebas, 0 fallos, 0 errores (40 previas + 14 nuevas)
```

## Qué cubren las 14 pruebas nuevas

| Caso | Prueba |
|---|---|
| Sin clave o clave incorrecta → 401 | `automationEndpointsRejectMissingOrWrongKey` |
| JWT ADMIN/USER → 403 | `userJwtRolesCannotUseAutomationEndpoints` |
| La clave no abre `/appointments/mine`, `/admin/inbox`, `/auth/me` | `automationKeyDoesNotOpenAnyOtherRoute` |
| Solo APPROVED dentro de la ventana (excluye REQUESTED, REJECTED, CANCELLED, COMPLETED, NO_SHOW, pasadas y fuera de ventana) | `onlyApprovedAppointmentsInsideTheWindowAreDue` |
| Sin documento, teléfono, motivo ni afiliación | `responseExposesOnlyMinimalFields` |
| `windowHours` 0 y 73 → 400 | `windowOutsideAllowedRangeIsRejected` |
| SENT idempotente, sale de la cola y un FAILED tardío no lo degrada | `sentDeliveryIsIdempotentAndRemovesTheAppointmentFromDue` |
| FAILED se reintenta hasta 3 intentos | `failedDeliveryIsRetriedUntilMaxAttempts` |
| Cita cancelada → 409, inexistente → 404 | `deliveryForAppointmentNoLongerApprovedConflicts` |
| Payload inválido no persiste nada | `deliveryPayloadIsValidated` |
| Ventana calculada con reloj UTC inyectado, límites y conteo de intentos | `AppointmentReminderServiceTest` (4) |
