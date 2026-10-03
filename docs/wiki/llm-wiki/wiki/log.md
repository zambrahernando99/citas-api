# Log de la LLM Wiki

- 2026-09-17 — INGEST — Corte inicial v1: README del workspace, PRD, restricciones técnicas, requisitos 3FN, evidencia y briefs n8n. Resultado: estructura, síntesis y preguntas abiertas iniciales creadas. Fuente: `../raw/README.md`.
- 2026-09-17 — LEARN — HU-001 y HU-002 aprobadas por el usuario; se registró DEC-003 y el contrato REST v1 de registro/sesión. Pendiente explícito: validación del consumidor `citas-web`.
- 2026-09-17 — LINT — Bootstrap e implementación de HU-001/HU-002 revisados contra contrato, decisión DEC-003 y pruebas. Resultado: backend validado con `mvn test` (9/9); no se registraron secretos y la validación cross-repo permanece pendiente.
- 2026-09-22 — LEARN — El usuario aprobó explícitamente las HU-001 a HU-028. Resultado: se actualizaron sus estados en el mapa Scrum; la aprobación habilita planificación e implementación, sin constituir evidencia de desarrollo, validación o completitud.
- 2026-09-22 — LINT — GOAL 01 y la integración S2 se verificaron con MySQL saludable, API/Flyway en ejecución, 9 pruebas backend en verde, CORS, registro, login, rotación, logout, typecheck y build web. Resultado: HU-001/HU-002 tienen evidencia cross-repo; no se persistieron secretos ni tokens en documentación.
- 2026-09-22 — LINT — Validación Docker integrada: MySQL, API Spring Boot y Vite levantados; el frontend recompiló en Linux y CORS permitió `http://localhost:5173`. Registro `201`, login con tokens y logout `204` confirmados con cuentas sintéticas.
- 2026-09-22 — LEARN — El usuario autorizó usar `database/reference/db.sql` para inicializar MySQL. Resultado: se creó DEC-005 y se configuró el montaje de solo lectura en Docker.
- 2026-09-22 — LINT — Volumen MySQL reinicializado desde la referencia: 3 roles, 15 usuarios, 3 citas y 224 slots cargados. Flyway estableció baseline `0` y aplicó V1; health API `200`, registro/login sintético y 9 pruebas backend en verde.
## 2026-09-24 — LEARN / S3 oferta profesional

- Clasificación: HECHO y DECISIÓN.
- Se implementaron HU-011 a HU-013: perfiles profesionales sintéticos, relación N:M de especialidades/sedes, especialidad primaria, estado activo y exclusión de directorio activo.
- Se registró DEC-006 y el contrato REST S3. No se añadieron bloques, reservas ni agenda de S4.
- Evidencia: pruebas backend y build/typecheck frontend en verde; el detalle está en `trazabilidad-y-calidad.md`.

## 2026-09-24 — LEARN / validación MySQL real S3

- Clasificación: HECHO.
- Docker Desktop inició MySQL 8.4 desde `database/reference/db.sql` en `citas_fcv_training`. Flyway V2 se aplicó correctamente tras renombrar una restricción S3 que colisionaba con el nombre de una restricción del esquema de referencia.
- El flujo REST real validó ADMIN, BCrypt, unicidad, N:M, especialidad primaria, sedes, estado activo y rechazo 403 para USER. Vite respondió en el frontend local.

## 2026-09-24 — LEARN / bloqueo de precondiciones S4

- Clasificación: PREGUNTA ABIERTA.
- El GOAL de concurrencia declara HU-014, HU-017 y HU-028 implementadas y contrato de reserva aprobado; la evidencia del checkout los mantiene pendientes y sólo documenta el contrato S3. Se registró la discrepancia en `riesgos-y-preguntas-abiertas.md`; no se implementó un contrato S4 por no inventar comportamiento observable.

- 2026-09-24 — LEARN — S3: se añadió el contrato de agenda, la migración V3 y pruebas de integración para bloques, slots, reserva y conflicto. HECHO: `mvn test` pasó con 17 pruebas usando el Maven local. PENDIENTE: validación del consumidor web y hook PASS tras reparar dependencias nativas de Node.

## 2026-09-29 — LEARN / incremento S4

- Clasificación: HECHO, DECISIÓN y PREGUNTA ABIERTA.
- Se implementó en ambos repos el ciclo de vida HU-014..026 y HU-028 en el código: bloques, consulta/búsqueda y reserva; cancelación y reprogramación con retención; agenda/cierre profesional; bandeja y decisiones ADMIN; perfil, recuperación, afiliación y catálogos EPS/planes/especialidades. La UI consume servicios REST; se retiraron las pantallas y fixtures de demostración.
- Decisiones registradas en DEC-007/008: no expirar retenciones; cierre sólo después de fin; token reset sólo en `dev`; regímenes configurables por ADMIN (excepción autorizada a HU-007); perfil editable limitado; cancelación bloqueada con reprogramación pendiente.
- Evidencia técnica ejecutada: `mvn -o -Dmaven.compiler.useIncrementalCompilation=false -DskipTests compile` pasó con Java release 21; `npm run lint` y `npm run build` pasaron. No se ejecutaron pruebas, migraciones en MySQL ni flujos funcionales reales en esta sesión; no afirmar completitud del DoD hasta aportar esa evidencia.
- S5 queda pendiente: no hay herramientas MCP de n8n ni instancia/credenciales conectadas. No se creó un JSON ficticio ni se activó una automatización.

## 2026-09-29 — LEARN / S3/S4 cierre

- Clasificación: HECHO, DECISIÓN y PREGUNTA ABIERTA.
- HU-003..HU-028 validadas con evidencia (backend 40, web 13 Vitest, E2E 47/47, loops 1/1/1).
- Contratos: historial de cita, bandeja admin, ids aditivos en `AppointmentResponse`, regímenes solo lectura, `not_found` 404 y `/error` permitido.
- Decisiones DEC-009..012: regímenes fijos, hook PowerShell sobre staged, volumen `web_node_modules` + perfil `dev`, Vitest.
- Riesgos: HMR en bind mount Windows, régimen SUB heredado, ADMIN por SQL, hook sobre árbol de trabajo.
- 2026-10-01 — LEARN — Enmienda a DEC-005: BD de la app separada de la de referencia (`citas_app` frente a `citas_fcv_training`) para que Flyway no falle en V3. Verificado con MySQL limpio: V1..V7 aplicadas y health UP.
- 2026-10-01 — LEARN — S5 backend: contrato de recordatorios WF-001, V8 `appointment_reminder_delivery`, filtro `X-Automation-Key` y decisiones DEC-013..017. Red → Green: 10/10 errores antes de implementar, luego suite 54/54. JSON WF-001 saneado (`active: false`) y hook de export n8n con FAIL/PASS. Pendiente: importar/ejecutar en la instancia n8n, credenciales Gmail OAuth y evidencia MCP.
- 2026-10-01 — LEARN — S6: WF-002 (outbox V9, despachador con reintentos, webhook n8n con respuesta 200/400/503) y WF-003 (endpoint diario sin PII, agrupación en n8n, correo de incidencia). Red 8/8 → suite 72/72. Workflows creados por MCP en la instancia (`lLZQOsvQpmVLXYah`, `Z8YOsBjSqA39K1pu`), probados con datos simulados y exportados a `automations/n8n/`. DEC-018..020; incidente de credencial ajena autoasignada corregido.
- 2026-10-03 — LEARN — WF-001 creado por MCP como `Hernando-WF-001-appointment-reminders` (`0LEn366VVamn82Ix`), mismo prefijo que WF-002/WF-003, sin credenciales; JSON versionado re-exportado desde la instancia.
