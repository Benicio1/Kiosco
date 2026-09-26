@echo off
title WinClean Lite
cd /d "%~dp0"
powershell -Command "Start-Process 'WinCleanLite.exe' -Verb runAs"
exit
