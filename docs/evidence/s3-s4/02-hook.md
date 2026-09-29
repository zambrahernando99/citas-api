# S3 — Hook local: FAIL / FAIL (secreto) / PASS — `citas-api`

Hook: `core.hooksPath=.githooks` → `.githooks/pre-commit` → `scripts/verify-s3.ps1`
1. Escaneo de secretos sobre el **contenido staged** (`git show :<archivo>`), patrón `(?i)(api[_-]?key|jwt[_-]?secret|password)\s*=\s*['"][^'"]{12,}`. Reporta solo `archivo:línea`, nunca el valor.
2. `mvn test` (Maven local `.tools`).

> Corrección previa: el escaneo original invocaba `rg` (ripgrep), que no está instalado en el host Windows; con `$ErrorActionPreference='Stop'` cualquier commit con archivos staged habría fallado por `CommandNotFoundException` en vez de por un hallazgo real. Se reemplazó por PowerShell nativo y se movió antes de las pruebas (falla rápida).

## FAIL 1 — pruebas en rojo
Commit de `AppointmentLifecycleIntegrationTest` antes de implementar:
```text
[ERROR] Tests run: 32, Failures: 3, Errors: 1, Skipped: 0
[INFO] BUILD FAILURE
exit=1 — commit rechazado (HEAD = da7866b)
```

## FAIL 2 — secreto ficticio
Se añadió `docs/evidence/s3-s4/fake-secret-demo.txt`, que asignaba a la clave jwt_secret un valor ficticio de 27 caracteres, y se intentó el commit S3:
```text
Potential secret pattern detected in staged content:
  docs/evidence/s3-s4/fake-secret-demo.txt:2
Potential secret pattern detected.
exit=1 — commit rechazado (HEAD = da7866b)
```
Corrección: `git rm --cached` + borrado del archivo. El valor ficticio **nunca** entró al historial.

## PASS
```text
[INFO] Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
exit=0 — commit 1744813 "test(s3): implement booking flow and automated quality gates"
```
