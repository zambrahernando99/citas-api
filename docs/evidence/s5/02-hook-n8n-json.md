# S5 — Hook del export n8n: FAIL y PASS

`scripts/verify-n8n-export.ps1` se ejecuta desde `scripts/verify-s3.ps1` (pre-commit) y revisa el contenido **staged** de `automations/n8n/*.json`. Nunca imprime el valor detectado.

Bloquea: JSON inválido, tokens OAuth, `client_secret`, `Bearer` literal, valor de `X-Automation-Key`, `data` dentro de `credentials`, `"active": true`, `pinData` con contenido y emails reales fijados (se permite `@example.test`).

## FAIL (2026-10-01)

Se preparó un archivo de sonda `automations/n8n/zz-hook-probe.json` con una clave **ficticia** (`fake-key-NOT-REAL-…`), datos de credencial, `active: true` y `pinData`. Se hizo `git add` y se ejecutó el script:

```text
Export de n8n bloqueado:
  automations/n8n/zz-hook-probe.json: valor de X-Automation-Key
  automations/n8n/zz-hook-probe.json: datos de credencial
  automations/n8n/zz-hook-probe.json: workflow activo
  automations/n8n/zz-hook-probe.json: pinData con contenido
n8n export contains forbidden content.
exit=1
```

La sonda se retiró del índice y se borró; nunca entró al historial.

## PASS

```text
git add automations/n8n/WF-001-appointment-reminders.json
Export n8n OK (1 archivo/s).
exit=0
```

## Nota

En este clon `core.hooksPath` no estaba configurado; para que el hook corra en cada commit: `git config core.hooksPath .githooks`. `verify-s3.ps1` ahora usa el Maven del PATH si no existe `.tools/`.
