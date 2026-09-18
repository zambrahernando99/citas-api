# Dominio

## HECHOS

- Actores: USER se registra y agenda; PROFESSIONAL es creado por ADMIN y gestiona disponibilidad; ADMIN gestiona catálogos, profesionales, solicitudes especializadas y reprogramaciones. Fuente: PRD §§2, 4.
- Las sedes HIC e ICV son catálogo fijo del laboratorio. Fuente: PRD §3.
- La disponibilidad se publica como bloques por profesional/sede y se discretiza en slots de 30 minutos. Una especialidad dura 30 o 60 minutos y la segunda opción requiere dos slots consecutivos. Fuente: PRD RF-08, RF-09.
- Una cita general se aprueba automáticamente; una especializada nace `REQUESTED` y exige decisión ADMIN. Fuente: PRD RF-11, RF-12.
- Cancelar, rechazar y reprogramar deben conservar las reglas de liberación o retención de slots y la auditoría de estado. Fuente: PRD RF-14, RF-15, RF-19; RN-09, RN-10.

## Límites

Historia clínica, pagos, facturación real, diagnósticos, datos reales e integraciones clínicas reales están fuera de alcance. Fuente: PRD §9.
