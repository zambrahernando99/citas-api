# S5 — Riesgos residuales (WF-001, n8n, MCP, ngrok)

Fecha: 2026-10-01. Alcance: endpoints `/api/v1/automation/**`, workflow WF-001, túnel ngrok, credenciales Gmail OAuth y servidor MCP de n8n. "Residual" significa lo que queda después de las mitigaciones aplicadas.

Escala: P = probabilidad, I = impacto (Baja / Media / Alta).

| # | Riesgo | P | I | Mitigación aplicada | Riesgo aceptado |
|---|---|---|---|---|---|
| R1 | Robo o filtración de `AUTOMATION_API_KEY` | Media | Media | Solo concede `ROLE_AUTOMATION` y solo en `/automation/**` (probado); comparación en tiempo constante; mínimo 32 caracteres; vive en `.env` y en la credencial de n8n; hook bloquea su valor en JSON | Quien tenga la clave puede leer citas próximas con nombre y email del paciente y marcar entregas. Rotación manual |
| R2 | Exposición pública por ngrok | Media | Media | `traffic-policy.yml` niega en el borde todo lo que no sea `/api/v1/automation/`; la clave sigue siendo obligatoria | La URL de ngrok es pública mientras el túnel está abierto; cerrarlo al terminar la sesión |
| R3 | PII (nombre y email) circulando por n8n y Gmail | Alta | Baja (datos sintéticos) | DEC-016 limita campos; `testRecipient` redirige a un buzón de laboratorio; la tabla de entregas no guarda emails | n8n guarda los datos de cada ejecución; con datos reales habría que limitar la retención de ejecuciones |
| R4 | Tokens OAuth de Gmail en n8n | Baja | Alta | Credencial propia por estudiante con scope `gmail.send`; el JSON solo referencia la credencial por nombre | Quien administre la instancia n8n puede usar esa credencial. Revocar el acceso en Google al terminar el curso |
| R5 | Inyección a través de respuestas MCP o datos de n8n | Media | Media | Política de contenido no confiable (`05-untrusted-content.md`); el agente no ejecuta órdenes incrustadas | Un agente con herramientas MCP de escritura podría actuar si la persona aprueba sin leer. Revisar cada acción de escritura |
| R6 | Inyección en el cuerpo del correo con datos de la cita | Baja | Baja | Correo en texto plano; no se envían motivos ni texto libre del paciente; nombres vienen de la API | Un nombre manipulado aparecería literal en el correo |
| R7 | Recordatorio duplicado o perdido | Baja | Baja | `UNIQUE (cita, ventana)`; SENT definitivo; FAILED con hasta 3 intentos; `providerMessageId` guardado | Si Gmail envía pero el POST de registro falla 3 veces, la siguiente ejecución reenvía (duplicado posible) |
| R8 | API no disponible | Media | Baja | 3 reintentos y rama `API no disponible` que marca la ejecución como error sin enviar | Los recordatorios de esa hora se envían en la siguiente ejecución si siguen dentro de la ventana |
| R9 | Zona horaria | Baja | Baja | API en UTC (`Clock.systemUTC`), correo formateado en `America/Bogota` | Si el reloj del host cambia, la ventana se desplaza |
| R10 | Activación sin prueba | Baja | Media | JSON exportado con `active: false`; el hook bloquea `active: true` | La activación en la instancia depende de la persona; activar solo tras la ejecución manual exitosa |
| R11 | Contenido de dependencias o issues que modifiquen el hook | Baja | Alta | Muestra y análisis en `05-untrusted-content.md`; el hook no descarga nada | Un cambio aprobado sin revisar el diff podría introducir código remoto |
| R12 | Límites de envío de Gmail | Baja | Baja | Máximo 200 citas por ejecución | Volúmenes reales requerirían otro proveedor |

## Fuera de alcance

- Datos reales de pacientes (el proyecto usa solo datos sintéticos).
- Endurecimiento de la instancia n8n del trainer (cuentas, copias, retención): es responsabilidad del trainer.
