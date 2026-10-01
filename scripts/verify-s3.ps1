$ErrorActionPreference = 'Stop'

# 1) Escaneo de secretos sobre el contenido staged (rápido; no imprime el valor detectado)
$pattern = '(?i)(api[_-]?key|jwt[_-]?secret|password)\s*=\s*[''"][^''"]{12,}'
$staged = @(git diff --cached --name-only --diff-filter=ACMR)
$scanned = @($staged | Where-Object { $_ -notin @('scripts/verify-s3.ps1', 'scripts/verify-n8n-export.ps1') })
$findings = @()
foreach ($file in $scanned) {
  $lines = @(git show ":$file" 2>$null)
  for ($i = 0; $i -lt $lines.Count; $i++) {
    if ($lines[$i] -match $pattern) { $findings += "${file}:$($i + 1)" }
  }
}
if ($findings.Count -gt 0) {
  Write-Host 'Potential secret pattern detected in staged content:' -ForegroundColor Red
  $findings | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
  throw 'Potential secret pattern detected.'
}

# 2) Export de n8n (S5): sin secretos, sin pinData y sin activar
& "$PSScriptRoot\verify-n8n-export.ps1"

# 3) Pruebas backend (Maven local del repo si existe; si no, el del PATH)
$localMaven = '.\.tools\apache-maven-3.9.9\bin\mvn.cmd'
if (Test-Path $localMaven) { & $localMaven '-Dmaven.repo.local=.tools/m2' test } else { & mvn -q test }
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
