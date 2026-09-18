# AGENTS.md — `citas-api`

## Estado verificado del repositorio

Al crear este archivo, el repositorio contiene documentación, ejemplos de entorno y briefs n8n, pero aún no tiene `pom.xml`, Maven Wrapper, código Java, migraciones Flyway ni pruebas. Existe el directorio `docs/wiki/scrum/`, pero no contiene especificaciones ni HU/DoD aprobadas. Por ello no se asumen paquetes Java, nombres de módulos, endpoints, perfiles, comandos de prueba ni HU aprobadas. Actualizar este apartado tras el bootstrap real de Spring Boot.

## Responsabilidad

Este repositorio es el único lugar para el backend del laboratorio: Java 21, Spring Boot 3.5.x, Maven, REST/JSON, Spring Security con JWT access/refresh, MySQL 8.4, Spring Data JPA, Flyway, reglas de negocio y pruebas backend.

No editar `citas-web` desde este repositorio ni introducir un BFF o acoplamiento a React/Angular. El frontend consume la API REST de Spring Boot directamente.

## Arquitectura requerida

- El dominio no depende de Spring, JPA, HTTP ni detalles de infraestructura.
- Los casos de uso viven en la capa de aplicación.
- Los puertos expresan dependencias de entrada y salida; REST y persistencia son adaptadores.
- Los controladores traducen HTTP, validan el borde y delegan en casos de uso: no concentran reglas de negocio.
- La persistencia se implementa mediante adaptadores Spring Data JPA; las migraciones del esquema se realizan con Flyway.

La estructura concreta de paquetes se decide al inicializar el proyecto y debe documentarse aquí con evidencia de árbol de código, sin inventarla antes.

## Reglas de producto que el backend debe preservar

- Los datos de laboratorio son sintéticos; no usar datos privados reales de FCV.
- Passwords se guardan con hash adaptativo; secretos llegan por variables de entorno y nunca por código, migraciones o logs.
- Access y refresh token son separados. No registrar passwords, tokens ni credenciales.
- La autorización se aplica por rol y ownership; CORS y validación server-side son explícitos.
- No se permiten bloques o citas en el pasado, bloques solapados ni reservas sobre slots ocupados o retenidos.
- Las especialidades duran 30 o 60 minutos; una de 60 requiere dos slots consecutivos.
- Las citas generales se aprueban automáticamente; las especializadas nacen `REQUESTED` y requieren decisión ADMIN. Un rechazo administrativo exige motivo.
- Cancelaciones y rechazos liberan la reserva correspondiente; una reprogramación pendiente conserva la cita original hasta su aprobación.
- Todo cambio de estado se audita y esa auditoría no se expone como CRUD normal.

Estas reglas provienen de `../PRD.md`; ante estados, retenciones, concurrencia o transiciones no definidos, no inferir el comportamiento: registrar el bloqueo para decisión.

## Base de datos

- Mantener como mínimo 3FN y justificar claves, cardinalidades y dependencias funcionales.
- Catálogos fijos se precargan por seed; EPS, planes y especialidades son configurables y no se borran físicamente si están referenciados.
- Cada cambio de esquema requiere una migración Flyway nueva, revisión de impacto y justificación en la HU o decisión correspondiente.
- No usar `../database/reference/` para dirigir el diseño inicial: es referencia posterior del trainer.

## Flujo de trabajo por cambio

1. Localizar la HU aprobada y su DoD en `docs/wiki/scrum/`. Si no existen, detener la implementación y solicitar esa aprobación.
2. Identificar reglas de PRD, datos, seguridad y contrato REST afectados.
3. Antes de editar, publicar un plan que enumere archivos backend, migraciones y pruebas previstos.
4. Implementar el mínimo coherente respetando límites hexagonales.
5. Ejecutar las pruebas relevantes disponibles. Tras el bootstrap, registrar aquí los comandos Maven reales y los perfiles requeridos.
6. Verificar arquitectura, migraciones, contrato y DoD; distinguir evidencia ejecutada de aspectos no verificados.
7. Si cambia REST, coordinar con el orquestador para actualizar contrato y exigir evidencia del consumidor web, sin modificar `citas-web` desde aquí.

## Documentación y automatizaciones

- `docs/wiki/llm-wiki/` es global y la mantiene el orquestador: no crear una wiki backend paralela ni modificarla salvo instrucción explícita de dicho rol.
- `docs/wiki/scrum/` es propiedad de la Skill Scrum; no convertirla en implementación de código.
- Los workflows n8n se exportan como JSON en `automations/n8n/`, sin credenciales ni secretos embebidos.
