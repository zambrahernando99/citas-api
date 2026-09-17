---
id: EP-004
tipo: epica
titulo: Reserva y consulta de citas
estado: Pendiente de aprobación
historias: ["[[HU-018-reservar-cita-general]]", "[[HU-019-solicitar-cita-especializada]]", "[[HU-020-consultar-mis-citas]]"]
dependencias: ["[[EP-003-oferta-profesional-y-disponibilidad]]", "[[EP-006-operacion-y-auditoria]]"]
---

# EP-004 — Reserva y consulta de citas

## Objetivo
Permitir que USER reserve cita general o solicite especializada y consulte sus resultados.

## Valor esperado
Convierte disponibilidad válida en citas sin doble reserva y con estados visibles.

## Actores
- USER
- ADMIN

## Alcance
- Reserva general autoaprobada, solicitud especializada retenida y mis citas.

## Fuera de alcance
- Facturación, pagos y atención clínica.

## Reglas de negocio
- General `APPROVED`; especializada `REQUESTED`; duración 30/60; slots consecutivos y sin doble reserva.

## Dependencias
- [[EP-003-oferta-profesional-y-disponibilidad]]
- [[EP-006-operacion-y-auditoria]]

## Historias de usuario
- [[HU-018-reservar-cita-general]]
- [[HU-019-solicitar-cita-especializada]]
- [[HU-020-consultar-mis-citas]]

## Criterio de completitud de la épica
- [ ] Todas las HUs obligatorias están `Completada` con evidencia.
- [ ] Las reservas creadas son trazables y no comparten slots.

## Riesgos e incógnitas
- Expiración de retenciones y tratamiento de Medicina General pendientes.
