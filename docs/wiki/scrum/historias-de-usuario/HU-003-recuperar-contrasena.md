---
id: HU-003
tipo: historia-de-usuario
titulo: Recuperar contraseña
estado: Aprobada
epica: "[[EP-001-acceso-e-identidad]]"
esfuerzo: Medio
sprint_sugerido: S2
dependencias: ["[[HU-001-registrar-user]]", "[[HU-002-gestionar-sesion-jwt]]"]
relacionadas: []
---
# HU-003 — Recuperar contraseña
## Historia de usuario
**COMO** usuario **QUIERO** recuperar mi contraseña con un token temporal **PARA** restaurar mi acceso.
## Contexto y descripción
El envío real es opcional; el desarrollo requiere alternativa segura aprobada.
## Alcance
- Solicitud, token de un uso y cambio de contraseña.
## Fuera de alcance
- SMTP obligatorio.
## Reglas de negocio
- Token temporal, único y consumido por cambio; password con hash adaptativo.
## Dependencias y relaciones
- Épica: [[EP-001-acceso-e-identidad]]
- Dependencias: [[HU-001-registrar-user]], [[HU-002-gestionar-sesion-jwt]].
- Relacionadas: ninguna.
## Esfuerzo
**Nivel:** Medio. **Justificación de dificultad:** token sensible y política segura de entrega.
## Tareas de desarrollo
- [x] **T-01 — Definir mecanismo seguro de desarrollo.** Dificultad: Medio. Resolver la incógnita de entrega.
- [x] **T-02 — Implementar token de un uso.** Dificultad: Medio. Persistir consumo/expiración y validar.
## Criterios de aceptación
### CA-01 — Solicitud
**Dado** una cuenta registrada **cuando** solicita recuperación **entonces** se crea un token temporal de un solo uso.
### CA-02 — Cambio válido
**Dado** token válido no usado **cuando** cambia contraseña **entonces** se actualiza con hash y el token se consume.
### CA-03 — Rechazo seguro
**Dado** token inválido, vencido o usado **cuando** cambia contraseña **entonces** no se modifica.
## Definition of Done
- [x] CA-01 a CA-03 validados con evidencia.
- [x] Contrato no expone password/token fuera del mecanismo aprobado.
- [x] Pruebas de un uso, invalidez y expiración disponibles; migración Flyway si aplica.
- [x] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Validado backend | `AccountAndCatalogIntegrationTest#passwordResetTokenIsSingleUseHashedAndReplacesPassword`, `#expiredPasswordResetTokenIsRejected` | Token SHA-256, TTL 900 s, un solo uso; sesiones revocadas |
| E2E | Validado | `docs/evidence/s3-s4/03-e2e-docker.md` (filas HU-003) | Token dev de un solo uso en perfil dev sin SMTP; contraseña anterior inválida |
| DoD | Validado | `docs/evidence/s3-s4/01-red-green.md`, `02-hook.md`, `docs/evidence/s3-s4/03-e2e-docker.md` | Backend 40 pruebas, web 13 Vitest, E2E 47/47 |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
- 2026-09-29 — Validada en S3/S4: evidencia en docs/evidence/s3-s4/ y docs/evidence/loops/.
## Notas y decisiones
- Mecanismo de desarrollo: token dev de un solo uso en perfil dev sin SMTP.
