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
