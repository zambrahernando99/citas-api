# Contratos REST

## HECHOS

- La API es REST/JSON directa entre `citas-web` y `citas-api`; no hay Express ni BFF. Fuente: PRD RF-20.
- Los contratos de HU-001 y HU-002 se implementan en el backend; la validación con el consumidor `citas-web` queda pendiente y no es evidencia completada. Fuente: HU-001, HU-002 y restricciones técnicas.

## DECISIÓN — Contrato de acceso e identidad v1

- Estado: aprobada.
- Fecha: 2026-09-17.
- Aprobó: usuario, al aprobar HU-001/HU-002 y solicitar su implementación.
- Alcance: únicamente registro `USER` y sesión JWT de HU-001/HU-002.
- Base: `/api/v1`.
- Media type: `application/json`.

### Registro

`POST /api/v1/auth/register`

Solicitud:

```json
{
  "givenNames": "...",
  "familyNames": "...",
  "documentType": "...",
  "documentNumber": "...",
  "email": "...",
  "phone": "...",
  "password": "..."
}
```

- Todos los campos son obligatorios y `email` debe tener formato básico válido.
- El cliente no envía ningún rol; el servidor asigna exclusivamente `USER`.
- No se imponen longitudes ni complejidad de contraseña fuera de exigir el campo.
- Respuesta: `201 Created` con `id`, datos de cuenta y `roles`; nunca devuelve `password` ni hashes.

### Sesión

`POST /api/v1/auth/login`

```json
{ "email": "...", "password": "..." }
```

`POST /api/v1/auth/refresh`

```json
{ "refreshToken": "..." }
```

`POST /api/v1/auth/logout`

```json
{ "refreshToken": "..." }
```

- Login y refresh válidos responden `200 OK` con `accessToken`, `refreshToken`, `accessTokenExpiresInSeconds` y `refreshTokenExpiresInSeconds`.
- Logout válido responde `204 No Content`.
- Los tokens se transportan sólo por el cuerpo de estos comandos; el access token se presenta después como `Authorization: Bearer <accessToken>`.
- El claim `roles` del access token forma el contexto de autorización. Este contrato no incorpora autorización por ownership ni endpoints de HU-004.

### Errores observables

Los errores usan `application/problem+json` con `status`, `code`, `detail` y, para validación, `fieldErrors`.

| Situación | Estado | `code` |
|---|---:|---|
| Campo obligatorio o email inválido | 400 | `validation_failed` |
| Email ya registrado | 409 | `email_already_registered` |
| Documento ya registrado | 409 | `document_already_registered` |
| Credenciales no válidas | 401 | `invalid_credentials` |
| Refresh malformado, inválido, expirado, revocado o reutilizado | 401 | `invalid_refresh_token` |
| Access ausente, inválido o vencido en un recurso protegido | 401 | `unauthorized` |
| Identidad válida sin autorización suficiente | 403 | `forbidden` |

Los detalles de refresh no distinguen causa para no revelar estado de sesión.

### CORS y autorización base

- CORS permite únicamente los orígenes configurados en `CORS_ALLOWED_ORIGINS`; no se usa comodín ni credenciales CORS implícitas.
- Son públicos sólo `POST /api/v1/auth/register`, `POST /api/v1/auth/login`, `POST /api/v1/auth/refresh`, `POST /api/v1/auth/logout` y el health de Actuator.
- Todo endpoint futuro requiere autenticación por defecto hasta que una HU aprobada defina reglas más específicas.

### Validación cross-repo S2

- 2026-09-22 — `citas-web` implementó el cliente directo de registro, login y logout mediante `VITE_API_URL`; no existe BFF ni almacenamiento persistente de tokens.
- 2026-09-22 — Con MySQL 8.4 y la API en ejecución se verificó el preflight CORS para `http://localhost:5173`, registro `201`, login con access/refresh, rotación de refresh y logout `204`, usando únicamente cuentas sintéticas de prueba.
- El frontend representa el `detail` de respuestas `application/problem+json`; los flujos de recuperación, ownership y las HU posteriores siguen fuera de alcance S2.

## DECISIÓN — Contrato de oferta profesional S3

- Estado: aprobado e implementado.
- Fecha: 2026-09-24.
- Base: `/api/v1`; JSON; el access token se presenta con `Authorization: Bearer`.
- Autorización: las mutaciones y la lista administrativa requieren `ADMIN`; los catálogos y el directorio activo requieren autenticación.

### Catálogos y directorio

- `GET /api/v1/catalogs/locations`: retorna las dos sedes fijas activas HIC e ICV (`id`, `code`, `name`, `address`, `active`).
- `GET /api/v1/specialties`: retorna únicamente especialidades activas (`id`, `code`, `name`, `durationMinutes`, `active`).
- `GET /api/v1/professionals`: retorna exclusivamente profesionales activos para consulta general.
- `GET /api/v1/admin/professionals`: retorna todos los perfiles para administración, incluso inactivos.

### Administración de profesionales

`POST /api/v1/admin/professionals`

```json
{
  "givenNames": "Profesional",
  "familyNames": "Sintético",
  "documentType": "CC",
  "documentNumber": "DOC-SINT-001",
  "email": "profesional.sintetico@example.test",
  "phone": "3000000000",
  "temporaryPassword": "solo-en-solicitud",
  "professionalCode": "PRO-SINT-001",
  "licenseNumber": "MAT-SINT-001"
}
```

- Responde `201 Created` con perfil, identificadores y asignaciones; no devuelve `temporaryPassword`, hash ni ningún secreto.
- El servidor almacena exclusivamente el hash BCrypt, asigna el rol `PROFESSIONAL` y exige unicidad de correo, documento, código y matrícula.

`PUT /api/v1/admin/professionals/{professionalId}/assignments`

```json
{
  "specialtyIds": [1, 2],
  "primarySpecialtyId": 2,
  "locationIds": [1, 2]
}
```

- Reemplaza las asignaciones N:M de forma atómica. Debe contener al menos una especialidad activa, sin repetidos, y la primaria debe pertenecer a la lista; se asigna una o ambas sedes fijas activas.

`PATCH /api/v1/admin/professionals/{professionalId}/active`

```json
{ "active": false }
```

- Conserva perfil y asignaciones. Un profesional inactivo no aparece en `GET /api/v1/professionals`.

### Errores S3

| Situación | Estado | `code` |
|---|---:|---|
| No autenticado / no ADMIN | 401 / 403 | `unauthorized` / `forbidden` |
| Correo o documento duplicado | 409 | `email_already_registered` / `document_already_registered` |
| Código o matrícula duplicados | 409 | `professional_code_already_registered` / `license_number_already_registered` |
| Perfil inexistente | 404 | `professional_not_found` |
| Especialidad inactiva/no asignada, primaria inválida o sede inválida | 400 | `invalid_professional_offer` |

S3 no expone ni implementa bloques de disponibilidad, reservas, slots ni agenda profesional.

## DECISIÓN — Contrato de agenda y reservas S3

- Estado: implementado; pendiente de validación cross-repo visible.
- `POST /api/v1/professional/availability-blocks`: PROFESSIONAL crea un bloque propio futuro, en una sede asignada y en múltiplos de 30 minutos.
- `GET /api/v1/availability?locationId&specialtyId&professionalId&date`: USER autenticado consulta slots reservables; para 60 minutos retorna únicamente inicios que tienen dos slots consecutivos.
- `POST /api/v1/appointments`: USER reserva con `professionalId`, `locationId`, `specialtyId` y `startsAt`. `MEDICINA_GENERAL` responde `201/APPROVED`; las demás especialidades responden `201/REQUESTED` y retienen sus slots.
- `GET /api/v1/admin/appointments/requested` y `POST /api/v1/admin/appointments/{id}/decision`: ADMIN consulta y decide. `approve=true` mantiene la retención; `approve=false` exige `reason`, cambia a `REJECTED` y libera los slots.
- Errores: `slot_unavailable` (`409`), `invalid_appointment` (`400`) y `appointment_not_found` (`404`), además de los errores de autenticación/rol existentes.
- La retención no vence automáticamente en S3: permanece hasta una decisión administrativa; la política de vencimiento sigue como pregunta abierta para una HU posterior.

## DECISIÓN — Contrato de ciclo de vida de citas S4

- Base `/api/v1`; JSON; access JWT por `Authorization: Bearer`.
- `GET /appointments/mine?status&from&to`: USER autenticado recibe únicamente sus citas, con profesional, sede, especialidad, inicio/fin, duración, estado, motivos disponibles y `reschedulePending`.
- `DELETE /appointments/{appointmentId}` con body opcional `{"reason":"..."}`: cancela una cita futura propia en `REQUESTED` o `APPROVED`, responde `204`, libera slots y registra `CANCELLED` en historial. No es reactivable por esta API. Si existe reprogramación `PENDING`, se bloquea hasta la decisión ADMIN; el frontend refleja ese estado y no ofrece cancelar.
- `POST /appointments/{appointmentId}/reschedules` con `{"startsAt":"...","reason":"..."}`: USER solicita nueva franja para una cita `APPROVED` propia y futura; conserva profesional, sede y especialidad, reserva los slots nuevos como `PENDING` y preserva la franja original. Responde `201` con el identificador y los datos de la solicitud. Retenciones sin vencimiento automático.
- `GET /admin/reschedules/pending`: ADMIN consulta solicitudes pendientes.
- `POST /admin/reschedules/{requestId}/decision` con `{"approve":true|false,"reason":"..."}`: ADMIN resuelve una vez. Aprobación intercambia slots y actualiza fecha/hora; rechazo exige motivo, libera slots nuevos y conserva cita original.
- Citas generales usan `MEDICINA_GENERAL` (ya sembrada en migración S3), son aprobadas al reservar y admiten cancelación; solicitudes especializadas nacen `REQUESTED`.
- Errores conservan el formato `application/problem+json`: `slot_unavailable` (`409`), `invalid_appointment` (`400`) y `appointment_not_found` (`404`, también para recursos ajenos o ya resueltos).
- Cambios de estado de cita registran actor, fuente y motivo cuando corresponde en `appointment_status_history`; solicitudes de reprogramación guardan su estado/decisión separadamente.
- `GET /professional/availability-blocks?from&to`: PROFESSIONAL autenticado consulta exclusivamente sus bloques por fecha.
- `PUT /professional/availability-blocks/{blockId}` con `{"locationId":1,"startsAt":"...","endsAt":"..."}` edita un bloque propio futuro y sin citas/retenciones.
- `DELETE /professional/availability-blocks/{blockId}` elimina un bloque propio futuro sin compromisos y libera su disponibilidad.
- `POST /professional/availability-blocks` deriva el profesional de la identidad autenticada; el body contiene sede e inicio/fin, nunca un `professionalId` seleccionable por cliente.

## DECISIÓN — Contrato de perfil, afiliación y catálogos

- `GET /api/v1/auth/me` y `PUT /api/v1/auth/me` leen/actualizan exclusivamente la cuenta del principal. El body permite `givenNames`, `familyNames`, `email` y `phone`; rol, documento y propiedad de otros usuarios no son editables. La respuesta de perfil no incluye documento ni hash.
- `GET /api/v1/eps`, `GET /api/v1/eps/{epsId}/plans` y `GET /api/v1/regimes` retornan catálogos activos. `GET/PUT /api/v1/profile/affiliation` administra la afiliación propia, validando relación plan-EPS y régimen activo.
- ADMIN administra EPS (`/admin/eps`), planes (`/admin/eps/{epsId}/plans`, `/admin/plans/{id}`), especialidades (`/admin/specialties`). Los regímenes son de solo lectura (`GET /api/v1/regimes` y `GET /api/v1/admin/regimes`); desde 2026-09-29 no hay mutaciones de regímenes. Las eliminaciones son lógicas; se conserva integridad referencial.
- **SUPERADA el 2026-09-29 (ver decisiones):** los regímenes serán configurables por ADMIN. Esta decisión autoriza expresamente una excepción al texto original de HU-007, que los describía como fijos/de solo lectura. HIC/ICV y roles/estados siguen siendo catálogos fijos/de solo lectura.
- **DECISIÓN aprobada por el usuario el 2026-09-24:** los únicos campos de perfil editables en HU-005 son nombres, apellidos, email y teléfono. La unicidad del email se conserva.

## DECISIÓN — Recuperación de contraseña

- `POST /api/v1/auth/password-reset-requests` acepta `{"email":"..."}` y responde `202 Accepted` de forma uniforme para cuentas existentes/inexistentes. El token es aleatorio, de un uso y con vencimiento configurable; sólo se persiste su huella hash.
- `POST /api/v1/auth/password-resets` acepta `{"token":"...","newPassword":"..."}`. Un token inválido, expirado o consumido responde `400 invalid_password_reset_token`; la contraseña se almacena con el hasher adaptativo existente.
- **DECISIÓN aprobada por el usuario el 2026-09-24:** el token se expone únicamente si el perfil Spring `dev` está activo y `PASSWORD_RESET_EXPOSE_TOKEN=true`. En otros entornos el API responde sin token; SMTP queda fuera de alcance conforme a HU-003.

## DECISIÓN — Agenda profesional y operación

- `GET /api/v1/professional/appointments?from&to&locationId` lista sólo citas `APPROVED` del profesional autenticado, con filtros inclusivos de fechas y sede.
- `PATCH /api/v1/professional/appointments/{appointmentId}/completion` acepta `{"status":"COMPLETED"}` o `{"status":"NO_SHOW"}` sólo cuando terminó la cita y pertenece al profesional. La decisión temporal fue aprobada por el usuario el 2026-09-24: el cierre se habilita únicamente después de la hora de fin.
- Una cancelación de cita con reprogramación `PENDING` asociada se rechaza hasta que ADMIN decida dicha solicitud; no se libera anticipadamente la franja propuesta.
- **DECISIÓN aprobada por el usuario el 2026-09-24:** las retenciones de solicitudes no expiran automáticamente. La retención de una cita `REQUESTED` termina por decisión ADMIN; la de la propuesta de reprogramación termina por decisión ADMIN.
- **DECISIÓN aprobada por el usuario el 2026-09-29:** si una cita tiene reprogramación `PENDING`, se bloquea su cancelación hasta la decisión ADMIN; la retención de la franja nueva no se libera por cancelación implícita.

## DECISIÓN — Ajustes de contrato S3/S4 (2026-09-29)

- Estado: implementado y validado (backend 40 pruebas, web 13 Vitest, E2E 47/47).
- `GET /api/v1/appointments/{id}/history`: lista de `HistoryResponse {status, source, changedAt, reason}` en orden cronológico; no expone el actor. Lo leen el dueño, el profesional asignado o ADMIN; cualquier otro recibe `404`. `PUT`/`DELETE` responden `405`; no hay mutación del historial.
- `GET /api/v1/admin/inbox?type&locationId&professionalId&specialtyId&from&to` (ADMIN): combina citas `REQUESTED` y reprogramaciones `PENDING`, sin efectos secundarios. `type` admite `APPOINTMENT` o `RESCHEDULE`. `InboxItemResponse`: `type`, `id`, `appointmentId`, `status`, `specialty`, `professionalName`, `location`, `patientName`, `startsAt`, `endsAt`, `previousStartsAt`, `reason`, `professionalId`, `specialtyId`, `locationId`.
- `AppointmentResponse` añade `professionalId`, `specialtyId` y `locationId` (cambio aditivo, compatible).
- Regímenes: solo `GET /api/v1/regimes` y `GET /api/v1/admin/regimes`; el CRUD admin fue retirado (PRD RF-05).
- Errores: `NoSuchElementException` → `404` con `code` `not_found`; `IllegalArgumentException` conserva `code` `invalid_professional_offer` pero `detail` usa el mensaje real de validación.
- `/error` está permitido en seguridad para conservar `404`/`405` reales; el `401` queda solo para peticiones sin token.

## DECISIÓN — Automatización de recordatorios WF-001 (S5, 2026-10-01)

- Estado: implementado y validado en backend (`AutomationReminderIntegrationTest` 10/10, `AppointmentReminderServiceTest` 4/4; suite 54/54). Consumidor: n8n, no `citas-web`; el contrato web no cambia.
- Autenticación: cabecera `X-Automation-Key` comparada en tiempo constante con `AUTOMATION_API_KEY` (mínimo 32 caracteres). Concede solo `ROLE_AUTOMATION` y solo en `/api/v1/automation/**`. Sin clave o clave incorrecta → `401`; JWT de USER/PROFESSIONAL/ADMIN → `403`; la clave en cualquier otra ruta → `401` (DEC-013).
- `GET /api/v1/automation/reminders/due?windowHours=24`: citas `APPROVED` con inicio en `(now, now + windowHours]` (UTC), sin entrega `SENT` para la ventana y con menos de `REMINDER_MAX_ATTEMPTS` (3) fallos. `windowHours` 1..72, por defecto `REMINDER_DEFAULT_WINDOW_HOURS`; fuera de rango → `400 invalid_reminder_request`. Máximo 200 por llamada, ordenadas por inicio.
  - Respuesta: `{ "windowCode": "H24", "generatedAt", "items": [ { "appointmentId", "startsAt", "endsAt", "locationName", "specialtyName", "professionalName", "patientFirstName", "patientEmail" } ] }`. No expone documento, teléfono, afiliación ni motivo (DEC-016).
- `POST /api/v1/automation/reminders/{appointmentId}/deliveries` con `{ "windowCode": "H24", "status": "SENT"|"FAILED", "channel"?: "GMAIL", "providerMessageId"?: string<=128, "errorCode"?: string<=64 }` → `200` con `{ appointmentId, windowCode, channel, status, attemptCount, providerMessageId, errorCode }`.
  - Idempotente por `(appointmentId, windowCode)`: un `SENT` repetido o un `FAILED` tardío no cambian un `SENT` existente; cada `FAILED` suma un intento (DEC-014).
  - Cita inexistente → `404 appointment_not_found`; cita que ya no está `APPROVED` → `409 reminder_not_applicable`; payload inválido → `400 validation_failed` o `invalid_reminder_request`.
