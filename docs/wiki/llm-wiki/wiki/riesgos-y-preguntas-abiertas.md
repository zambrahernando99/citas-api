# Riesgos y preguntas abiertas

- **PREGUNTA ABIERTA:** grafo completo de estados, terminalidad y transiciones permitidas de cita y reprogramación.
- **RESUELTO — retenciones S4:** no hay vencimiento automático; las retenciones `REQUESTED` y `PENDING` se mantienen hasta decisión ADMIN. La cita original no se puede cancelar mientras haya reprogramación pendiente.
- **PREGUNTA ABIERTA:** mecanismo de concurrencia verificable para reservas simultáneas.
- **PREGUNTA ABIERTA:** usuarios con múltiples roles y selección del contexto de autorización.
- **PREGUNTA ABIERTA:** valores completos y gobierno de cambios de catálogos fijos.
- **PREGUNTA ABIERTA:** anticipación para crear bloques y su edición cuando hay retenciones, no solo citas comprometidas.
- **PENDIENTE DE VERIFICACIÓN — concurrencia S4:** reserva y reprogramación usan bloqueos de slots, y el checkout compila; faltan pruebas concurrentes y ejecución contra MySQL antes de considerar verificadas las garantías en runtime.
- **PENDIENTE S5 — integración externa:** no hay cliente/servidor MCP de n8n disponible en las herramientas de esta sesión ni instancia/credenciales de n8n configuradas. No se puede invocar ni evidenciar un workflow funcional sin esa precondición del trainer.
- **RIESGO:** elegir React/Angular antes de la aprobación visual Stitch/AI Studio contradice el flujo del curso.
- **RIESGO:** convertir briefs n8n en contratos o activar flujos sin payload, autenticación y prueba controlada.
