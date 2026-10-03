# Riesgos y preguntas abiertas

- **PREGUNTA ABIERTA:** grafo completo de estados, terminalidad y transiciones permitidas de cita y reprogramación.
- **RESUELTO — retenciones S4:** no hay vencimiento automático; las retenciones `REQUESTED` y `PENDING` se mantienen hasta decisión ADMIN. La cita original no se puede cancelar mientras haya reprogramación pendiente.
- **PREGUNTA ABIERTA:** mecanismo de concurrencia verificable para reservas simultáneas.
- **PREGUNTA ABIERTA:** usuarios con múltiples roles y selección del contexto de autorización.
- **PREGUNTA ABIERTA:** valores completos y gobierno de cambios de catálogos fijos.
- **PREGUNTA ABIERTA:** anticipación para crear bloques y su edición cuando hay retenciones, no solo citas comprometidas.
- **PENDIENTE DE VERIFICACIÓN — concurrencia S4:** reserva y reprogramación usan bloqueos de slots, y el checkout compila; faltan pruebas concurrentes y ejecución contra MySQL antes de considerar verificadas las garantías en runtime.
- **PENDIENTE S5 — integración externa (actualizado 2026-10-01):** instancia y servidor MCP identificados (`impulso-n8n.aiacademy.com.co`) y conector n8n agregado a la cuenta del usuario; backend, JSON y hook listos. Falta ejecutar WF-001 en la instancia y la invocación MCP. Riesgos residuales: `citas-api/docs/security/S5-riesgos-residuales.md`.
- **RIESGO:** elegir React/Angular antes de la aprobación visual Stitch/AI Studio contradice el flujo del curso.
- **RIESGO:** convertir briefs n8n en contratos o activar flujos sin payload, autenticación y prueba controlada.
- **HECHO/RIESGO — Vite HMR en Windows:** Vite no detecta cambios sobre el bind mount en Windows; hay que reiniciar el dev server para ver cambios.
- **HECHO/RIESGO — régimen heredado:** la BD dev conserva un régimen `SUB`/subsidiado creado antes de V7; no afecta al seed fijo, pero puede aparecer en consultas locales.
- **HECHO/RIESGO — alta de ADMIN:** no existe alta pública de ADMIN; en dev el rol se asigna por SQL.
- **HECHO/RIESGO — hook:** el hook ejecuta las pruebas sobre el árbol de trabajo, no solo sobre lo staged; cambios no staged pueden influir en el resultado.
