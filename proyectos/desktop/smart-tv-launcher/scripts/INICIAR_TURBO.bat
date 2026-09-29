@echo off
title Smart TV Launcher - Modo Turbo 4GB RAM
cd /d "%~dp0\.."

echo ====================================================
echo   INICIANDO MODO TURBO (OPTIMIZADO PARA 4 GB RAM)
echo ====================================================

:: Ejecutar optimizacion de memoria en PowerShell
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0\optimizar_windows.ps1"

:: Iniciar servidor Node.js
start /b "" node server.mjs

:: Esperar a que el servidor inicialice
timeout /t 1 /nobreak >nul

:: Buscar navegador ligero
set BROWSER=msedge.exe
where msedge.exe >nul 2>&1
if errorlevel 1 (
  set BROWSER=chrome.exe
)

echo Iniciando %BROWSER% en modo Kiosco Ultra Liviano...
start "" %BROWSER% --app=http://localhost:3000 --remote-debugging-port=9222 --start-fullscreen --disable-web-security --user-data-dir="%TEMP%\smart_tv_profile" --disable-extensions --disable-background-networking --disable-sync --disable-default-apps

echo ====================================================
echo   SMART TV TURBO ACTIVO.
echo ====================================================
