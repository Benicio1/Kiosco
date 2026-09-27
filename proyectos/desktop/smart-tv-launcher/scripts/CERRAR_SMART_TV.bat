@echo off
title Cerrando Smart TV Launcher...

echo Deteniendo Smart TV y liberando recursos...

:: Detener proceso de Node.js del launcher
taskkill /F /IM node.exe >nul 2>&1

echo [OK] Servidor detenido correctamente.
timeout /t 2 >nul
