# Estado Actual (Parte de Turno)

> **CÓMO SE USA ESTE ARCHIVO (REGLA OPERATIVA):**  
> 1. Es el **parte de turno del proyecto** (máximo 1 página).  
> 2. Se lee al iniciar cada sesión para saber exactamente en qué estado quedó el trabajo.  
> 3. **Se actualiza obligatoriamente al terminar cada sesión de trabajo.**  
> *Versión Actual:* `v1.4.0` | *Última actualización:* 2026-09-29

---

### 1. ¿Qué funciona hoy?
* **Nuevo Proyecto: Lector Confort PDF para Teléfono Móvil (`proyectos/web/lector-pdf-pwa`):**
  - **Selector Local de PDFs:** Permite abrir archivos PDF almacenados en el teléfono mediante selector táctil o arrastrar/soltar sin conexión a internet.
  - **Filtros de Luz Antifatiga Visual (6 Modos):**
    - ☀️ **Normal (Día):** Nitidez y colores originales balanceados.
    - 🌅 **Luz Cálida (Anti-Azul):** Filtro ámbar de 2700K para bloquear la luz azul antes de dormir.
    - 📜 **Sepia (Papel Libro):** Emulación de página de libro impreso con textura suave.
    - 🌙 **Modo Noche (Carbón):** Inversión suave para lectura en la oscuridad sin reflejos.
    - ⬛ **OLED (Negro Puro #000000):** Apagado total de píxeles en pantallas AMOLED/OLED y ahorro máximo de batería.
    - 📄 **e-Ink (Tinta Electrónica):** Escala de grises con alto contraste mate tipo lector de libros electrónicos.
  - **Panel de Ajuste Fino:** Deslizadores en tiempo real para Brillo (25% a 130%), Calidez/Ámbar (0% a 100%) y Contraste (70% a 140%).
  - **Experiencia Táctil e Inmersiva:** Modo inmersión (tap central para ocultar barras de navegación), deslizamiento swipe para cambiar de página, zoom dinámico y ajuste al ancho.
  - **Biblioteca y Memoria de Progreso:** Guarda automáticamente la última página leída y porcentaje de cada libro en `localStorage`.
  - **PWA 100% Offline:** Service Worker y Web App Manifest con estrategia Cache-First e iconos adaptativos para instalar en la pantalla principal del teléfono.
  - **Servidor Wi-Fi y Launcher:** `server.mjs` con autodetección de IPs locales y lanzador `INICIAR_LECTOR.bat`.
  - **Calidad y Tests:** 11/11 pruebas unitarias superadas (`npm test`) y estricto cumplimiento del límite de 400 líneas.

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
  - **Sistema Unificado de Ratón con Detección Automática de Contexto (App / Crunchyroll / Windows):**
    - **Libertad Total del Cursor de la Computadora:** Se eliminó cualquier bloqueo o `cursor: none`. El cursor nativo de Windows está siempre visible y se mueve libremente tanto si el usuario mueve el ratón físico de su computadora como si utiliza el touchpad táctil del celular.
    - **Detección Automática de Estado en Tiempo Real (`/api/status`):** El sistema detecta automáticamente mediante CDP si el usuario está en el menú de la Smart TV (`tv_app`), navegando en Crunchyroll (`crunchyroll`) o en el escritorio/otros programas (`windows`), actualizando el indicador en el control del celular en tiempo real ("📺 En Smart TV", "🟠 Activo en Crunchyroll", "💻 Modo Computadora").
    - **Pipeline de Ratón Ultra-Rápido sin Acumulación de Colas HTTP:** En `remote.js`, el envío táctil acumula deltas en una sola petición en vuelo a la vez (`flushMouseMove`), eliminando el lag o retraso de 2 fps por encolamiento HTTP en Wi-Fi.
    - **Control Unificado con Win32 `InputBridge.exe`:** Los eventos de movimiento, clics y desplazamiento pasan directo a nivel hardware (`mouse_event`), garantizando cero cursores dobles y clics 100% certeros en cualquier ventana.
  - **Buscador Inteligente de Animes y Videos desde el Teléfono Móvil:**
    - **Búsqueda Directa en Crunchyroll:** Al escribir un anime en el buscador del celular y presionar 🔍 o Enter, el servidor navega automáticamente a `https://www.crunchyroll.com/es/search?q={query}` vía CDP, mostrando los resultados en la TV al instante.
    - **Selector Rápido de Destino:** Botones en el celular `[🤖 Auto]`, `[🟠 Crunchyroll]` y `[▶️ YouTube]` para elegir dónde buscar con un solo toque desde cualquier pantalla.
    - **Apertura Inmediata del Teclado del Celular:** Al tocar la lupita 🔍 en el teléfono, el campo de texto se enfoca abriendo automáticamente el teclado táctil nativo.
    - **Placeholder Dinámico en Tiempo Real:** El control cambia automáticamente su texto de ayuda según la app activa (`🟠 Buscar anime en Crunchyroll...` / `▶️ Buscar video en YouTube...`).
  - **Restauración de Modo Estable para YouTube TV y Conmutación a Crunchyroll:**
    - **Modo Estable Restaurado:** Se volvió al User-Agent oficial verificado (`BRAVIA 4K UR2 / Android 10`) y parámetros limpios en `INICIAR_SMART_TV.bat` / `INICIAR_TURBO.bat`, eliminando el cartel de *"Se produjo un error. Se debe reiniciar la app de YouTube"*.
    - **Calidad Estable:** YouTube TV vuelve a reproducir fluidamente a 720p nativo sin cortes ni incompatibilidades.
    - **Conmutación Directa a Crunchyroll:** Se mantiene activa la mejora que permite conmutar directamente a Crunchyroll desde el control del celular en cualquier momento, limpiando el User-Agent para que Crunchyroll cargue en versión web de escritorio sin problemas.
    - **Modularidad Arquitectónica:** `server.mjs` (314 líneas) y `bridge.mjs` (75 líneas), cumpliendo de forma holgada con la Regla 2 (< 400 líneas).
  - **Verificación:** 11/11 pruebas superadas en `tv.test.mjs`, cumplimiento estricto del límite de 400 líneas en todos los archivos.

### 2. ¿Qué se está haciendo ahora?
* Todo el código está restaurado al estado estable probado y verificado.

### 3. ¿Qué está bloqueado / decisiones pendientes?
* Ningún bloqueo técnico.

### 4. Próximo paso inmediato
* El usuario ejecuta `CERRAR_TV.bat` e inicia nuevamente con `INICIAR_TV.bat` (o `INICIAR_TURBO.bat`) para volver a ver YouTube TV de forma fluida y estable.
