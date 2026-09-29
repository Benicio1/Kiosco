# Estado Actual (Parte de Turno)

> **CÓMO SE USA ESTE ARCHIVO (REGLA OPERATIVA):**  
> 1. Es el **parte de turno del proyecto** (máximo 1 página).  
> 2. Se lee al iniciar cada sesión para saber exactamente en qué estado quedó el trabajo.  
> 3. **Se actualiza obligatoriamente al terminar cada sesión de trabajo.**  
> *Versión Actual:* `v1.3.1` | *Última actualización:* 2026-09-26

---

### 1. ¿Qué funciona hoy?
* **Corrección de Diseño y Nombres 100% Visibles en Celular (v1.3.1):**
  - **Rediseño Móvil en 2 Filas Espaciosas:** El nombre del producto ya no compite horizontalmente con el stepper `[- 1 caja +]`. Fila superior dedicada al estado, nombre completo (`word-break: break-word`, `font-size: 16px`, sin truncamiento) y acciones. Fila inferior para metadatos y control de cantidad.
  - **Legibilidad Total de Productos en Stock:** Los productos no faltantes se leen perfectamente en plateado/blanco nítido sin line-through excesivo ni opacidades que tapen el texto.
  - **Botón "Añadir Ahora" a Ancho Completo:** La caja de añadir rápida no se corta ni desborda en pantallas pequeñas.
* **PWA Instalable con Cartel Superior e Iconos Nativos (v1.3.0):**
  - **Cartel flotante interactivo superior ("📲 Instalar Kiosco App"):** Botón directo para instalar en 1 toque.
  - **Iconos PWA Nativos:** Iconos de 192x192, 512x512 y Apple Touch Icon para pantalla de inicio.
  - **Manifest y Service Worker:** `manifest.json` y `sw.js` activos con caché y soporte offline.
* **Modo Autónomo Móvil (Independiente de la Computadora):**
  - **Enlace Nube en Vivo:** `https://benicio1.github.io/Kiosco/` activo y sincronizado.
* **Calidad y Verificación Operativa:**
  - 17/17 pruebas automatizadas superadas (`npm test`).
  - Límite estricto de 400 líneas respetado en todos los archivos de `src/`.

* **Nuevo Proyecto: Smart TV Launcher Ultra-Liviano (`proyectos/desktop/smart-tv-launcher`):**
  - **Crunchyroll en la Misma Ventana (Sin Ventanas Secundarias):** Se restableció la carga directa en la misma ventana de la TV (`window.location.href`), eliminando cualquier apertura de ventanas superpuestas.
  - **Retroceso y Regreso al Launcher Corregidos (`KEYEVENTF_EXTENDEDKEY`):**
    - Se identificó la causa raíz por la que Windows no retrocedía: en Win32 `keybd_event`, las teclas de dirección (`VK_LEFT`) y multimedia (`VK_BROWSER_BACK`) exigen obligatoriamente la bandera `KEYEVENTF_EXTENDEDKEY (0x0001)`; de lo contrario Windows las enviaba como teclado numérico (`Alt + Numpad 4`).
    - Ahora `SendBrowserBack()` inyecta `Alt + Flecha Izquierda Extendida` y `VK_BROWSER_BACK`, permitiendo retroceder de inmediato en el historial de Crunchyroll y regresar al Launcher.
    - El botón **Inicio (Home)** ejecuta retrocesos en ráfaga para regresar directo a `localhost:3000` en la misma pantalla sin abrir ningún navegador nuevo.
    - Los scripts `.bat` ahora usan `--start-fullscreen` en vez de `--kiosk` para que el motor de Chromium no inhabilite los atajos de retroceso.
  - **Barra de Navegación Rápida en el Touchpad Móvil:** Botones directos `[↩️ Atrás]` y `[🏠 Menú TV]` accesibles sin cambiar de pestaña.
  - **Verificación:** 8/8 pruebas superadas en `tv.test.mjs`.

### 2. ¿Qué se está haciendo ahora?
* Todo el código está implementado, probado y compilado.

### 3. ¿Qué está bloqueado / decisiones pendientes?
* Ningún bloqueo técnico.

### 4. Próximo paso inmediato
* El usuario ejecuta `CERRAR_SMART_TV.bat` y luego `INICIAR_TURBO.bat` para probar el retroceso dentro de la misma ventana de Crunchyroll.
