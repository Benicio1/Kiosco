# 🚀 WinClean Lite — Optimizador Ligero para Windows 10/11

## Propósito
Herramienta nativa ultra liviana (<40 KB) para resolver problemas de saturación de memoria RAM (90%-99%) y uso intensivo de CPU/disco en netbooks y PCs de recursos moderados.

## Incluye
- `WinCleanLite.cs`: Código fuente en C# (.NET Framework 4.0/4.8) con llamadas a APIs Win32 (`EmptyWorkingSet`, manipulación de servicios y registro).
- `WinCleanLite.exe`: Binario ejecutable portable compilado sin dependencias externas.
- `app.manifest`: Manifiesto con solicitud de elevación de privilegios de Administrador (`requireAdministrator`).
- `Ejecutar_WinCleanLite.bat`: Script de conveniencia para ejecución directa.
- `LEEME.txt`: Documentación original de referencia.

## No incluye
- Librerías externas pesadas ni instaladores MSI/InnoSetup (es 100% portable).

## Funcionalidades Principales
1. **Purga Inmediata de RAM:** Vía API Win32 `EmptyWorkingSet` sobre procesos del sistema y usuario sin cerrarlos.
2. **Limpieza de Temporales:** `%TEMP%`, `Windows\Temp`, `SoftwareDistribution\Download`, `CrashDumps`, miniaturas y papelera.
3. **Optimizaciones de Windows 11:**
   - Desactivación de Widgets/Noticias (`WebView2`).
   - Desactivación de búsquedas web en menú inicio (Bing Search).
   - Detención de Telemetría (`DiagTrack`) y `SysMain`.
   - Ajuste de rendimiento visual y eliminación de sugerencias comerciales.
4. **Optimización en 1-Clic:** Rutina desatendida completa.

## Compilación Manual
```powershell
& "C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe" /target:winexe /win32manifest:app.manifest /optimize+ /r:System.Windows.Forms.dll /r:System.Drawing.dll /out:WinCleanLite.exe WinCleanLite.cs
```

## Relación con el Workspace MÖLDEA
- [AGENTS.md](../../../AGENTS.md)
- [proyectos/desktop/README.md](../README.md)
- [docs/GUIA_DOCUMENTACION.md](../../../docs/GUIA_DOCUMENTACION.md)
