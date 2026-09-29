# LOOP 03 — Reto independiente: reconciliación de contrato frontend/backend

| # | Elemento exigido | Definición |
|---|---|---|
| 1 | Disparador | Tras S3 el backend cambió el contrato (regímenes fijos sin CRUD, `/admin/inbox`, `/appointments/{id}/history`, IDs de referencia). `tsc` y `vite build` quedaron rotos y la UI no consumía lo nuevo. |
| 2 | Meta verificable | `npm run lint` 0 errores, `npm test` 100 %, `npm run build` OK en `citas-web-dev` y `mvn test` 100 % en `citas-api-dev`, con los 7 criterios de UI cumplidos |
| 3 | Estado observado / persistente | Salidas de los 4 comandos y `git diff` de `citas-web` (persisten en el árbol de trabajo entre iteraciones) |
| 4 | Alcance del Builder | Solo `citas-web/src/**` y `citas-web/AGENTS.md`. Sin dependencias nuevas, sin tocar `vite.config.ts` ni el backend |
| 5 | Evidencia del Verifier | Subagente aislado de solo lectura: diff, greps de mocks/secretos/referencias retiradas, contraste de rutas contra controladores Spring, ejecución de los 4 comandos |
| 6 | Presupuesto | Máximo 3 iteraciones |
| 7 | Condición de parada | Meta verificable cumplida y `VERDICT: PASS` |
| 8 | Escalamiento humano | Si hace falta cambiar el contrato backend, una dependencia o la config de Vite → `ESCALATE` y decisión humana |
| 9 | Log | Esta página + `LOOP_03-contract-reconciliation.json` |
| 10 | Por qué no bastaba un prompt | Hay 7 frentes acoplados (catálogos, bandeja, agenda, navegación, sesión, docs, pruebas) sobre dos archivos grandes compartidos. Cada frente puede romper el typecheck o las pruebas de otro, y la correctitud depende de un contrato externo (Spring) que conviene verificar con alguien que no implementó. El ciclo con Verifier aislado y presupuesto acotado evita declarar "listo" solo porque compila. |

## Iteración 1
- **Builder:** los 7 objetivos implementados (ver JSON).
- **Resultados:** `tsc` 0 errores · Vitest 2 archivos, 13/13 · build OK · backend 39/39.
- **Verifier:** `VERDICT: PASS`. Observación no bloqueante: funciones `requestedAppointments`/`pendingReschedules` sin uso. El orquestador las eliminó después del loop.

**Resultado:** COMPLETED en 1/3 iteraciones.
