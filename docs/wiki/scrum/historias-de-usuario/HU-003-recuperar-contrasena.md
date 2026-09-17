---
id: HU-003
tipo: historia-de-usuario
titulo: Recuperar contraseña
estado: Pendiente de aprobación
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
- [ ] **T-01 — Definir mecanismo seguro de desarrollo.** Dificultad: Medio. Resolver la incógnita de entrega.
- [ ] **T-02 — Implementar token de un uso.** Dificultad: Medio. Persistir consumo/expiración y validar.
## Criterios de aceptación
### CA-01 — Solicitud
**Dado** una cuenta registrada **cuando** solicita recuperación **entonces** se crea un token temporal de un solo uso.
### CA-02 — Cambio válido
**Dado** token válido no usado **cuando** cambia contraseña **entonces** se actualiza con hash y el token se consume.
### CA-03 — Rechazo seguro
**Dado** token inválido, vencido o usado **cuando** cambia contraseña **entonces** no se modifica.
## Definition of Done
- [ ] CA-01 a CA-03 validados con evidencia.
- [ ] Contrato no expone password/token fuera del mecanismo aprobado.
- [ ] Pruebas de un uso, invalidez y expiración disponibles; migración Flyway si aplica.
- [ ] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Pendiente | — | Entrega segura pendiente |
| DoD | Pendiente | — | — |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
## Notas y decisiones
- Bloqueada para desarrollo hasta decidir mecanismo de entrega seguro.
