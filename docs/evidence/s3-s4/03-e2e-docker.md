# S3/S4 — Validación E2E sobre la instancia Docker

Fecha: 2026-09-29 · Servicios `fcv-citas-mysql` (MySQL 8.4, healthy), `fcv-citas-api-dev` (Java 21, `mvn spring-boot:run`, perfil `dev`), `fcv-citas-web-dev` (Node 24, Vite en :5173).
Todos los datos son sintéticos (`e2e-*@example.test`, contraseñas aleatorias que no se registran). El rol ADMIN del usuario E2E se asignó por SQL en la BD de desarrollo, porque no existe alta pública de administradores.

## 0. Infraestructura

| Verificación | Resultado |
|---|---|
| `docker compose ps` | 3 servicios Up; MySQL `healthy` |
| `GET /actuator/health` | `200 {"status":"UP"}` |
| Flyway en MySQL | V0 baseline + V1..V7 con `success=1` (V7 `seed fixed regimes` aplicada en esta sesión) |
| `mvn test` en `citas-api-dev` | 40 ejecutadas, 0 fallas, BUILD SUCCESS |
| `npm run lint` / `npm test` / `npm run build` en `citas-web-dev` | tsc OK · Vitest 2 archivos 13/13 · build OK |

### Ajustes de infraestructura detectados y corregidos (`docker-compose.yml` raíz)
1. **`node_modules` compartido host/contenedor:** estaba instalado desde Windows, así que `tsc` y `vite build` fallaban en Linux (`@typescript/typescript-linux-x64` y el binding de rolldown ausentes). Se añadió el volumen `web_node_modules:/workspace/node_modules` y se ejecutó `npm ci` en el contenedor.
2. **Recuperación de contraseña sin SMTP:** el contenedor API no activaba el perfil `dev`, por lo que RF-03 no se podía completar. Se añadió `SPRING_PROFILES_ACTIVE=dev` y `PASSWORD_RESET_EXPOSE_TOKEN=true`, con valores por defecto solo para el contenedor de desarrollo.
3. **HMR en bind mount de Windows:** Vite en el contenedor no detecta cambios de archivos; tras editar hay que reiniciar `npm run dev` (se documenta en el AGENTS web).

## 1. E2E de API sobre MySQL real (script Node, 47 verificaciones)

| Resultado | Verificación | Detalle |
|---|---|---|
| PASS | Registro admin | HTTP 201 |
| PASS | Login ADMIN (rol asignado por SQL en BD dev) |  |
| PASS | Registro usera | HTTP 201 |
| PASS | Registro userb | HTTP 201 |
| PASS | Login USER A y USER B |  |
| PASS | USER → /admin/inbox = 403 |  |
| PASS | Sin token → /appointments/mine = 401 |  |
| PASS | HU-007 regímenes fijos precargados (V7) | CONTRIBUTIVO,ESPECIAL,EXCEPCION,SUB |
| PASS | HU-007 POST /admin/regimes bloqueado |  |
| PASS | HU-008/009 ADMIN crea EPS y plan |  |
| PASS | HU-006 afiliación USER A | EPS E2E mun4bnqs · Plan E2E · Contributivo |
| PASS | HU-011/012 ADMIN crea profesional y asigna especialidades/sede | MEDICINA_GENERAL+CARDIOLOGIA @ HIC |
| PASS | Login PROFESSIONAL |  |
| PASS | HU-014 bloque 08:00–10:00 |  |
| PASS | HU-014 bloque solapado rechazado |  |
| PASS | HU-014 USER no crea bloques (403) |  |
| PASS | HU-017 slots 30 min = 4, inicios 60 min consecutivos = 3 | 30:4 60:3 |
| PASS | HU-018 cita general → APPROVED |  |
| PASS | Doble reserva mismo slot → 409 |  |
| PASS | HU-019 especializada 60 min → REQUESTED |  |
| PASS | Slot retenido por REQUESTED → 409 |  |
| PASS | HU-027 bandeja contiene la REQUESTED y no la general |  |
| PASS | HU-023 rechazo sin motivo → 400 |  |
| PASS | HU-023 rechazo con motivo → REJECTED |  |
| PASS | Rechazo libera slots: USER B reserva 09:30 |  |
| PASS | HU-022 reprogramación → PENDING |  |
| PASS | HU-022 segunda solicitud con PENDING → 409 (defecto LOOP_01) |  |
| PASS | HU-022 nueva franja retenida → 409 para otro USER |  |
| PASS | HU-027 bandeja filtra RESCHEDULE |  |
| PASS | HU-024 aprobación mueve la cita a 09:00 y sigue APPROVED | 2026-10-02T09:00:00 |
| PASS | HU-020 IDs de referencia en mis citas |  |
| PASS | Slot 08:00 liberado tras aprobar reprogramación |  |
| PASS | HU-021 USER A no cancela cita de B → 404 |  |
| PASS | HU-021 USER B cancela su cita → 204 |  |
| PASS | HU-025 agenda profesional solo APPROVED propias | 2 citas |
| PASS | HU-026 cerrar cita futura → 404 |  |
| PASS | HU-026 cierre NO_SHOW de cita terminada → 204 |  |
| PASS | HU-028 historial REQUESTED→REJECTED con fuente y motivo | REQUESTED/USER,REJECTED/ADMIN |
| PASS | HU-028 USER B no lee historial de A → 404 |  |
| PASS | HU-028 reprogramación aprobada auditada |  |
| PASS | HU-003 token dev de un solo uso (perfil dev, sin SMTP) |  |
| PASS | HU-003 contraseña anterior inválida / nueva válida |  |
| PASS | MySQL: 0 slots ligados a citas cerradas/rechazadas/canceladas | 0 |
| PASS | MySQL: 0 retenciones de reprogramaciones ya decididas | 0 |
| PASS | MySQL: toda cita E2E tiene historial | 0 |
| PASS | MySQL: sin slots duplicados | 0 |
| PASS | Flyway en V7 | 7 |

La primera corrida dio 46/47. `POST /admin/regimes` devolvía **401** en vez de 405, porque el despacho interno a `/error` pasaba por seguridad sin JWT. Se corrigió permitiendo `/error` en `SecurityConfiguration`, con la regresión `ErrorDispatchIntegrationTest` sobre un servidor real: roja antes del fix, verde después. Segunda corrida: **47/47**.

Nota de datos: la BD de desarrollo ya tenía un régimen `SUB/subsidiado` creado a mano con el CRUD anterior. El seed idempotente V7 no duplica ese nombre, por lo que el régimen subsidiado sigue expuesto con código `SUB`.

## 2. E2E de interfaz (navegador → http://localhost:5173)

| Actor | Flujo | Resultado |
|---|---|---|
| USER | Login, menú por rol (sin Profesionales/Especialidades), estados en español | OK |
| USER | Mis citas: cita rechazada con motivo; historial "Solicitada · Paciente → Rechazada · Administración" | OK |
| USER | Reprogramar: búsqueda con IDs reales devuelve 08:30 y 09:30 libres; envío → badge "Reprogramación pendiente", original 09:00 conservada, acciones deshabilitadas | OK |
| ADMIN | Bandeja "Reprogramaciones" con filtros; rechazar sin motivo → "El motivo de rechazo es obligatorio."; aprobar → "Solicitud aprobada." y recarga | OK |
| ADMIN | EPS y planes: "Regímenes (catálogo fijo)" de solo lectura; EPS/planes con CRUD | OK |
| PROFESSIONAL | Mi agenda: cita reprogramada a 09:30; "Atendida/No asistió" deshabilitados con aviso "Podrás cerrar la atención cuando la cita haya terminado." | OK |
