$ErrorActionPreference = 'Stop'

# S5: los JSON exportados de n8n no pueden llevar secretos, datos fijados ni quedar activos.
# Revisa el contenido staged de automations/n8n/*.json y nunca imprime el valor detectado.
$rules = [ordered]@{
  'token OAuth'                 = '(?i)"(access|refresh|id)_?token"\s*:'
  'secreto de cliente OAuth'    = '(?i)"client_?secret"\s*:'
  'cabecera Bearer literal'     = '(?i)Bearer\s+[A-Za-z0-9._~+/-]{16,}'
  'valor de X-Automation-Key'   = '(?i)X-Automation-Key"?\s*[:=]\s*"[^"{=][^"]{7,}'
  'datos de credencial'         = '(?i)"credentials"\s*:\s*\{[^}]*"data"\s*:'
  'workflow activo'             = '"active"\s*:\s*true'
  'pinData con contenido'       = '"pinData"\s*:\s*\{\s*"'
  'email real fijado'           = '(?i)"(sendTo|testRecipient|value)"\s*:\s*"[^"={]+@(?!example\.test)[^"]+"'
}
$staged = @(git diff --cached --name-only --diff-filter=ACMR | Where-Object { $_ -like 'automations/n8n/*.json' })
$findings = @()
foreach ($file in $staged) {
  $content = (@(git show ":$file" 2>$null) -join "`n")
  try { $null = $content | ConvertFrom-Json } catch { $findings += "${file}: JSON inválido" }
  foreach ($rule in $rules.GetEnumerator()) {
    if ($content -match $rule.Value) { $findings += "${file}: $($rule.Key)" }
  }
}
if ($findings.Count -gt 0) {
  Write-Host 'Export de n8n bloqueado:' -ForegroundColor Red
  $findings | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
  throw 'n8n export contains forbidden content.'
}
if ($staged.Count -gt 0) { Write-Host "Export n8n OK ($($staged.Count) archivo/s)." -ForegroundColor Green }
