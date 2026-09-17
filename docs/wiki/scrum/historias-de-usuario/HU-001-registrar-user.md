---
id: HU-001
tipo: historia-de-usuario
titulo: Registrar USER
estado: Aprobada
epica: "[[EP-001-acceso-e-identidad]]"
esfuerzo: Medio
sprint_sugerido: S2
dependencias: []
relacionadas: ["[[HU-002-gestionar-sesion-jwt]]"]
---
# HU-001 — Registrar USER
## Historia de usuario
**COMO** visitante  
**QUIERO** crear una cuenta USER con datos mínimos  
**PARA** acceder al agendamiento ficticio.
## Contexto y descripción
Solicita nombres, apellidos, tipo/número de documento, email, teléfono y contraseña; no permite autoasignar roles.
## Alcance
- Registro USER y validación de datos/duplicados.
## Fuera de alcance
- Creación de PROFESSIONAL o ADMIN.
## Reglas de negocio
- Email y documento únicos; password nunca en texto plano.
## Dependencias y relaciones
- Épica: [[EP-001-acceso-e-identidad]]
- Dependencias: ninguna.
- Relacionadas: [[HU-002-gestionar-sesion-jwt]].
## Esfuerzo
**Nivel:** Medio. **Justificación de dificultad:** valida identidad, unicidad y seguridad entre contrato, dominio y persistencia.
## Tareas de desarrollo
- [x] **T-01 — Definir contrato y validaciones.** Dificultad: Medio. Documentar datos requeridos y errores observables.
- [x] **T-02 — Implementar registro persistente.** Dificultad: Medio. Aplicar hash, unicidades y pruebas relevantes.
## Criterios de aceptación
### CA-01 — Registro válido
**Dado** un visitante con todos los datos mínimos válidos **cuando** se registra **entonces** se crea una cuenta con rol USER.
### CA-02 — Unicidad
**Dado** email o documento existente **cuando** intenta registrar otra cuenta **entonces** se rechaza sin crearla.
### CA-03 — Protección de contraseña
**Dado** cualquier registro **cuando** se persiste o responde **entonces** la contraseña no aparece en texto plano.
## Definition of Done
- [x] CA-01 a CA-03 validados con evidencia backend.
- [x] Contrato REST y validación server-side coherentes.
- [ ] UI consume REST directo. Pendiente de validación en `citas-web`; fuera de este cambio.
- [x] Migración Flyway coherente y pruebas relevantes disponibles.
- [x] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Validado backend | `AuthFlowIntegrationTest#registersUserWithOnlyUserRoleAndNeverReturnsOrPersistsRawPassword` | Registro 201 y rol exclusivo USER |
| CA-02 | Validado backend | `AuthFlowIntegrationTest#rejectsDuplicateEmailAndDocumentWithoutCreatingAnotherAccount` | 409 sin segunda cuenta |
| CA-03 | Validado backend | Misma prueba CA-01 y `RegistrationServiceTest` | BCrypt en persistencia y sin password en respuesta |
| DoD backend | Validado | `mvn test`: 9 pruebas, 0 fallos | UI/cross-repo permanecen pendientes |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
- 2026-09-17 — Aprobada para implementación por autorización explícita del usuario.
- 2026-09-17 — Implementación backend validada mediante `mvn test`; consumo UI y validación cross-repo pendientes.
## Notas y decisiones
- Datos exclusivamente sintéticos.
