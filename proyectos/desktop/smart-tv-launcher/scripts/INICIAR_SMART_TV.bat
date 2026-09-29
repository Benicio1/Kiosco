@echo off
title Smart TV Launcher - Iniciando...
cd /d "%~dp0\.."

echo ====================================================
echo   INICIANDO SMART TV LAUNCHER (MODO ESTANDAR)
echo ====================================================

:: Detectar Node.js (Portatil en bin\ o instalado en el sistema)
set "NODE_CMD=node"
if exist "%~dp0\..\bin\node.exe" (
  set "NODE_CMD=%~dp0\..\bin\node.exe"
)

:: Iniciar servidor Node.js en segundo plano
start /b "" "%NODE_CMD%" server.mjs

:: Esperar 1 segundo para que el servidor este listo
timeout /t 1 /nobreak >nul

:: Buscar Microsoft Edge o Chrome para modo Kiosco a Pantalla Completa
set BROWSER=msedge.exe
where msedge.exe >nul 2>&1
if errorlevel 1 (
  set BROWSER=chrome.exe
)

echo Abriendo Pantalla de TV en %BROWSER%...
start "" %BROWSER% --app=http://localhost:3000 --remote-debugging-port=9222 --start-fullscreen --disable-web-security --user-data-dir="%TEMP%\smart_tv_profile" --disable-pinch --window-size=1920,1080 --ignore-gpu-blocklist --enable-gpu-rasterization --enable-zero-copy

echo ====================================================
echo   SMART TV EN EJECUCION.
echo   Para cerrar, ejecuta scripts\CERRAR_SMART_TV.bat
echo ====================================================
