@echo off
chcp 65001 >nul
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0Commitear.ps1"
if %ERRORLEVEL% NEQ 0 (
    echo.
    pause
)
