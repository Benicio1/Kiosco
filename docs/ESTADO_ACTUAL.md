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
  - **Control Remoto Directo del Navegador vía CDP (Chrome DevTools Protocol):**
    - Se solucionó de raíz el problema por el cual el navegador en modo quiosco (`--app`) ignoraba los atajos de teclado de Windows al estar en Crunchyroll.
    - Se configuró el puerto `--remote-debugging-port=9222` en los scripts de inicio (`INICIAR_TURBO.bat` e `INICIAR_SMART_TV.bat`).
    - Al presionar **🏠 Inicio (Home)** o **Menú TV** en el celular, el servidor Node.js emite la orden directa `Page.navigate` a Chromium vía WebSocket, devolviendo la TV a `http://localhost:3000/` en menos de 200 ms, sin importar en qué página de Crunchyroll esté el usuario.
    - Al presionar **↩️ Atrás**, ejecuta `window.history.back()` nativo vía CDP y, si la página no tiene historial previo o no logra salir, fuerza el retorno a `http://localhost:3000/` automáticamente.
  - **Crunchyroll TV Hub (Intermediario) y Login Web Oficial:**
    - Catálogo nativo interactivo con One Piece, Jujutsu Kaisen, Demon Slayer, etc., trailers oficiales en TV y botón `[🌐 Abrir Crunchyroll Web (Con tu Cuenta)]`.
    - Inicio de sesión 100% funcional sin restricciones de iframe ni bloqueos de Cloudflare Turnstile.
  - **Cierre Limpio del Sistema (`CERRAR_SMART_TV.bat` / `CERRAR_TV.bat`):**
    - Finaliza procesos de Node.js, `InputBridge.exe` y la ventana del navegador quiosco de forma atómica.
  - **Verificación:** 8/8 pruebas superadas en `tv.test.mjs` y pruebas E2E de navegación CDP validadas con éxito.

### 2. ¿Qué se está haciendo ahora?
* Todo el código está implementado, probado y compilado.

### 3. ¿Qué está bloqueado / decisiones pendientes?
* Ningún bloqueo técnico.

### 4. Próximo paso inmediato
* El usuario ejecuta `CERRAR_TV.bat` (o `CERRAR_SMART_TV.bat`) y luego `INICIAR_TURBO.bat` para verificar que tanto **🏠 Inicio** como **↩️ Atrás** lo devuelven inmediatamente al menú desde Crunchyroll sin cerrar el programa.
