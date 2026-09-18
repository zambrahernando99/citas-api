# Datos y normalización

## HECHOS

- El modelo debe alcanzar 3FN y representar usuarios/roles, profesionales, especialidades y sedes N:M, afiliación, bloques, citas, estados, historial, reprogramaciones y tokens compatibles con el PRD. Fuente: requisitos 3FN.
- No se admiten listas en columnas, dependencias parciales en claves compuestas ni dependencias transitivas de atributos no clave. Fuente: requisitos 3FN.
- Catálogos fijos se cargan por seed; EPS, planes y especialidades son configurables y no se borran físicamente si están referenciados. Fuente: PRD RF-05, RF-06.

## PREGUNTAS ABIERTAS

- Estrategia concreta de concurrencia, índice/restricción y transacción para evitar doble reserva.
- Representación física de slots y de las retenciones pendientes.
- Datos por snapshot frente a FK en citas y reprogramaciones.

No usar `database/reference/` como fuente de diseño antes de la comparación autorizada por el trainer.
