@echo off
title Cerrando Smart TV Launcher...

echo Deteniendo Smart TV y liberando recursos...

:: Detener proceso de Node.js del launcher
taskkill /F /IM node.exe >nul 2>&1
taskkill /F /IM InputBridge.exe >nul 2>&1

:: Cerrar ventana del navegador del launcher
powershell -NoProfile -Command "Get-CimInstance Win32_Process | Where-Object { $_.CommandLine -like '*smart_tv_profile*' } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue }" >nul 2>&1

echo [OK] Servidor y Smart TV detenidos correctamente.
timeout /t 2 >nul
