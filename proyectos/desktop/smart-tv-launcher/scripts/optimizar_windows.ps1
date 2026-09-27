# optimizar_windows.ps1 - Liberador de Recursos para Netbook de 4 GB RAM

Write-Host ">>> Optimizando Windows 11 para Modo Smart TV..." -ForegroundColor Cyan

# 1. Lista de procesos parásitos de fondo para cerrar si están abiertos
$procesosACerrar = @("OneDrive", "Teams", "Discord", "Spotify", "Cortana", "Widgets")

foreach ($proc in $procesosACerrar) {
    $found = Get-Process -Name $proc -ErrorAction SilentlyContinue
    if ($found) {
        Write-Host "[-] Cerrando proceso pesado en segundo plano: $proc" -ForegroundColor Yellow
        Stop-Process -Name $proc -Force -ErrorAction SilentlyContinue
    }
}

# 2. Forzar recolección de basura del sistema y vaciado de Working Set
[System.GC]::Collect()
[System.GC]::WaitForPendingFinalizers()

Write-Host "[+] Limpieza de RAM completada con exito." -ForegroundColor Green
