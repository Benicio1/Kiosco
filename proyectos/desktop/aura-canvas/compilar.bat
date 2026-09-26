@echo off
cd /d "%~dp0"
echo Compilando AuraPaint (Arquitectura Modular en src\)...
"C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe" /target:winexe /out:AuraCanvasMVP.exe /optimize+ /r:System.Windows.Forms.dll /r:System.Drawing.dll /r:"C:\Windows\Microsoft.NET\assembly\GAC_64\PresentationCore\v4.0_4.0.0.0__31bf3856ad364e35\PresentationCore.dll" /r:"C:\Windows\Microsoft.NET\assembly\GAC_MSIL\WindowsBase\v4.0_4.0.0.0__31bf3856ad364e35\WindowsBase.dll" /r:System.Xaml.dll /recurse:src\*.cs
if %ERRORLEVEL% equ 0 (
    echo [EXITO] AuraCanvasMVP.exe compilado correctamente.
) else (
    echo [ERROR] Fallo la compilacion.
    pause
)
