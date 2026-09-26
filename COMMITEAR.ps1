<#
.SYNOPSIS
    Ayuda a hacer un commit de git paso a paso, sin memorizar comandos.
.DESCRIPTION
    Muestra que cambio, pide confirmacion antes de git add, arma el mensaje
    con el tipo estandar (FEAT, FIX, REFACTOR, DOCS, CHORE) y commitea con confirmacion.
#>
[CmdletBinding()]
param()

$raiz = $PSScriptRoot
Push-Location $raiz

function Salir {
    param([string]$Mensaje, [int]$Codigo = 0)
    Write-Host ""
    Write-Host "  $Mensaje" -ForegroundColor Yellow
    Pop-Location
    exit $Codigo
}

if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    Write-Host "  [X] git no esta instalado o no esta en el PATH." -ForegroundColor Red
    Pop-Location
    exit 1
}

# Inicializar git si no existe
if (-not (Test-Path (Join-Path $raiz '.git'))) {
    Write-Host ""
    Write-Host "  [i] No se encontro un repositorio git en esta carpeta." -ForegroundColor Yellow
    $crearGit = Read-Host "  Deseas inicializar git ahora? (S/n)"
    if ($crearGit -ne 'n' -and $crearGit -ne 'N') {
        git init
        Write-Host "  [OK] Repositorio Git inicializado." -ForegroundColor Green
    } else {
        Salir "Operacion cancelada."
    }
}

Write-Host ""
Write-Host ("=" * 70) -ForegroundColor DarkCyan
Write-Host "  COMMITEAR - Antigravity full organizado" -ForegroundColor Cyan
Write-Host ("=" * 70) -ForegroundColor DarkCyan

# 1. Que cambio
$cambios = git status --porcelain
if (-not $cambios) {
    Salir "No hay nada para commitear: el proyecto esta igual que el ultimo commit."
}

Write-Host ""
Write-Host "  Esto cambio desde el ultimo commit:" -ForegroundColor White
Write-Host ""
git status --short

Write-Host ""
$verDiff = Read-Host "  Ver el detalle linea por linea de los cambios? (s/N)"
if ($verDiff -eq 's' -or $verDiff -eq 'S') {
    git --no-pager diff
    git --no-pager diff --stat
}

# 2. Add
Write-Host ""
$confirmarAdd = Read-Host "  Marcar TODOS estos cambios para el commit? (S/n)"
if ($confirmarAdd -eq 'n' -or $confirmarAdd -eq 'N') {
    Salir "Cancelado. No se marco ni se commiteo nada."
}

git add -A

Write-Host ""
Write-Host "  Quedo marcado para commitear:" -ForegroundColor Green
git status --short

# 3. Tipo de cambio
Write-Host ""
Write-Host "  Que tipo de cambio es?" -ForegroundColor White
Write-Host "    [1] feat      - Funcionalidad nueva"
Write-Host "    [2] fix       - Correccion de un bug o error"
Write-Host "    [3] refactor  - Reorganizacion de codigo sin cambiar lo que hace"
Write-Host "    [4] docs      - Solo documentacion (README, guias, notas)"
Write-Host "    [5] chore     - Tareas de mantenimiento, configs, dependencias"
Write-Host "    [6] style     - Formato, espacios, CSS visual"
Write-Host ""
$opcion = Read-Host "  Elegi una opcion (1-6)"

$tipo = switch ($opcion) {
    '1' { 'feat' }
    '2' { 'fix' }
    '3' { 'refactor' }
    '4' { 'docs' }
    '5' { 'chore' }
    '6' { 'style' }
    default { 'chore' }
}

# 4. Modulo o alcance
Write-Host ""
$alcance = Read-Host "  Modulo o parte que tocaste (opcional, ej: core, ui, auth - Enter para omitir)"

# 5. Mensaje
Write-Host ""
$resumen = Read-Host "  Escribi un resumen breve de lo que hiciste"
while ([string]::IsNullOrWhiteSpace($resumen)) {
    Write-Host "  [!] El mensaje no puede estar vacio." -ForegroundColor Yellow
    $resumen = Read-Host "  Escribi un resumen breve de lo que hiciste"
}

# Construir mensaje final
if ([string]::IsNullOrWhiteSpace($alcance)) {
    $mensajeFinal = "$tipo" + ": $resumen"
} else {
    $mensajeFinal = "$tipo($alcance): $resumen"
}

Write-Host ""
Write-Host ("-" * 70) -ForegroundColor DarkGray
Write-Host "  Mensaje final del commit:" -ForegroundColor White
Write-Host "  $mensajeFinal" -ForegroundColor Green
Write-Host ("-" * 70) -ForegroundColor DarkGray

Write-Host ""
$confirmarCommit = Read-Host "  Confirmar y commitear? (S/n)"
if ($confirmarCommit -eq 'n' -or $confirmarCommit -eq 'N') {
    Salir "Commit cancelado. Los cambios quedaron marcados (staged)."
}

git commit -m $mensajeFinal

Write-Host ""
Write-Host "  [OK] Commit realizado con exito." -ForegroundColor Green
Pop-Location
