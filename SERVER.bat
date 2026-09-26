@echo off
chcp 65001 >nul
title Servidor Local - Antigravity full organizado
echo [i] Levantando servidor local para ver el avance del proyecto...
node "%~dp0herramientas\servidor_local.mjs"
if %ERRORLEVEL% NEQ 0 (
    echo.
    pause
)
