# Índice — LLM Wiki global

## Estado

Inicializada el 2026-09-17. Las HU-001 a HU-028 fueron aprobadas para planificación e implementación el 2026-09-22. S3 tenía contrato y backend; S4 amplió ciclo de vida, agenda profesional, recuperación, perfil, afiliación y catálogos con su consumidor web REST y sin fixtures. El 2026-09-29 se cerró S3/S4: HU-003..HU-028 validadas (backend 40 pruebas, web 13 Vitest, E2E Docker 47/47, loops 1/1/1). Las decisiones de producto están en DEC-007..012 (DEC-009 fija los regímenes). El 2026-10-01 se implementó el backend de S5 (contrato de recordatorios WF-001, V8, DEC-013..017; suite 54/54) y el JSON de WF-001; falta la ejecución en n8n y la evidencia MCP. La BD de la app es `citas_app`, separada de la referencia `citas_fcv_training` (enmienda a DEC-005). Desde 2026-09-22, MySQL se inicializa además desde `database/reference/db.sql` por decisión explícita del usuario.

## Páginas

- [Dominio](dominio.md): actores, capacidades y reglas de negocio.
- [Arquitectura](arquitectura.md): límites de repositorio y stack obligatorio.
- [Datos y normalización](datos-y-normalizacion.md): restricciones 3FN y pendientes de diseño.
- [Contratos REST](contratos-rest.md): contratos v1 implementados, incluidos historial y bandeja admin.
- [Decisiones](decisiones.md): decisiones aprobadas.
- [Riesgos y preguntas abiertas](riesgos-y-preguntas-abiertas.md): asuntos que no se resuelven por inferencia.
- [Trazabilidad y calidad](trazabilidad-y-calidad.md): Git, pruebas y evidencia.
- [Automatizaciones n8n](automatizaciones-n8n.md): alcance S5/S6.
- [Log](log.md): registro cronológico append-only.

## Fuentes

Ver [inventario RAW](../raw/README.md) y las convenciones en [schema](../schema/convenciones.md).
