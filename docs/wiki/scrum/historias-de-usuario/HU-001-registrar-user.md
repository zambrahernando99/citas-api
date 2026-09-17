---
id: HU-001
tipo: historia-de-usuario
titulo: Registrar USER
estado: Pendiente de aprobación
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
- [ ] **T-01 — Definir contrato y validaciones.** Dificultad: Medio. Documentar datos requeridos y errores observables.
- [ ] **T-02 — Implementar registro persistente.** Dificultad: Medio. Aplicar hash, unicidades y pruebas relevantes.
## Criterios de aceptación
### CA-01 — Registro válido
**Dado** un visitante con todos los datos mínimos válidos **cuando** se registra **entonces** se crea una cuenta con rol USER.
### CA-02 — Unicidad
**Dado** email o documento existente **cuando** intenta registrar otra cuenta **entonces** se rechaza sin crearla.
### CA-03 — Protección de contraseña
**Dado** cualquier registro **cuando** se persiste o responde **entonces** la contraseña no aparece en texto plano.
## Definition of Done
- [ ] CA-01 a CA-03 validados con evidencia.
- [ ] Contrato REST y validación server-side coherentes; UI consume REST directo.
- [ ] Migración Flyway coherente si el modelo requiere nuevas estructuras; pruebas relevantes disponibles.
- [ ] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 a CA-03 | Pendiente | — | HU no implementada |
| DoD | Pendiente | — | — |
## Historial de validación
- 2026-09-17 — Recreada con skill en `Pendiente de aprobación`.
## Notas y decisiones
- Datos exclusivamente sintéticos.
