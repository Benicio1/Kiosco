@echo off
title Smart TV Launcher - Iniciando...
cd /d "%~dp0\.."

echo ====================================================
echo   INICIANDO SMART TV LAUNCHER (MODO ESTANDAR)
echo ====================================================

:: Iniciar servidor Node.js en segundo plano
start /b "" node server.mjs

:: Esperar 1 segundo para que el servidor este listo
timeout /t 1 /nobreak >nul

:: Buscar Microsoft Edge o Chrome para modo Kiosco a Pantalla Completa
set BROWSER=msedge.exe
where msedge.exe >nul 2>&1
if errorlevel 1 (
  set BROWSER=chrome.exe
)

echo Abriendo Pantalla de TV en %BROWSER%...
start "" %BROWSER% --kiosk --app=http://localhost:3000 --disable-pinch --overscroll-history-navigation=0

echo ====================================================
echo   SMART TV EN EJECUCION.
echo   Para cerrar, ejecuta scripts\CERRAR_SMART_TV.bat
echo ====================================================
