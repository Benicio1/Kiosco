@echo off
:: Comprobar si se ejecuta como Administrador
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo Solicitando permisos de administrador...
    powershell -Command "Start-Process cmd -ArgumentList '/c `"%~f0`"' -Verb RunAs"
    exit /b
)

cd /d "%~dp0"
echo ===================================================
echo   Instalando Driver Qualcomm Atheros AR9271...
echo ===================================================
echo.
pnputil /add-driver "%~dp0netathurx.inf" /install
echo.
if %errorlevel% equ 0 (
    echo [OK] Driver instalado correctamente. Tu antena Wi-Fi ya deberia estar activa.
) else (
    echo [!] Hubo un aviso o error con codigo: %errorlevel%
)
echo.
pause
