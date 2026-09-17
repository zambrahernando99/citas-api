---
id: HU-002
tipo: historia-de-usuario
titulo: Gestionar sesión JWT
estado: Pendiente de aprobación
epica: "[[EP-001-acceso-e-identidad]]"
esfuerzo: Alto
sprint_sugerido: S2
dependencias: ["[[HU-001-registrar-user]]"]
relacionadas: ["[[HU-004-autorizar-por-rol-y-propiedad]]"]
---
# HU-002 — Gestionar sesión JWT
## Historia de usuario
**COMO** usuario registrado **QUIERO** iniciar, renovar y cerrar sesión **PARA** acceder con una sesión segura.
## Contexto y descripción
Login por email/contraseña; access y refresh separados, con refresh revocable.
## Alcance
- Login, refresh y logout.
## Fuera de alcance
- Proveedor externo de identidad.
## Reglas de negocio
- Roles presentes en autorización; secretos solo en environment; no loguear tokens/passwords.
## Dependencias y relaciones
- Épica: [[EP-001-acceso-e-identidad]]
- Dependencias: [[HU-001-registrar-user]].
- Relacionadas: [[HU-004-autorizar-por-rol-y-propiedad]].
## Esfuerzo
**Nivel:** Alto. **Justificación de dificultad:** seguridad transversal, revocación y contrato frontend/backend.
## Tareas de desarrollo
- [ ] **T-01 — Documentar contrato de sesión.** Dificultad: Medio. Definir respuestas y errores sin fijar secretos.
- [ ] **T-02 — Implementar emisión y revocación.** Dificultad: Alto. Separar tokens y probar flujos negativos.
## Criterios de aceptación
### CA-01 — Login
**Dado** credenciales válidas **cuando** inicia sesión **entonces** recibe access y refresh separados.
### CA-02 — Renovación segura
**Dado** refresh válido **cuando** renueva **entonces** recibe la renovación definida; uno inválido/revocado/expirado se rechaza.
### CA-03 — Logout
**Dado** una sesión activa **cuando** hace logout **entonces** el refresh queda invalidado según contrato.
## Definition of Done
- [ ] CA-01 a CA-03 validados con evidencia.
- [ ] CORS y autorización base explícitos; secretos no versionados ni registrados.
- [ ] Pruebas de login, refresh, revocación y token inválido disponibles.
- [ ] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Pendiente | — | HU no implementada |
| DoD | Pendiente | — | — |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
## Notas y decisiones
- No se fijan duraciones/rotación de token sin decisión.
