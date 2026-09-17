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

### Pendiente cross-repo

- `citas-web` debe verificar payloads, CORS y manejo de errores contra esta implementación. Pendiente: no verificado ni implementado en este cambio.
