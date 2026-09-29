# Estado Actual (Parte de Turno)

> **CÓMO SE USA ESTE ARCHIVO (REGLA OPERATIVA):**  
> 1. Es el **parte de turno del proyecto** (máximo 1 página).  
> 2. Se lee al iniciar cada sesión para saber exactamente en qué estado quedó el trabajo.  
> 3. **Se actualiza obligatoriamente al terminar cada sesión de trabajo.**  
> *Versión Actual:* `v1.5.0` | *Última actualización:* 2026-09-29

---

### 1. ¿Qué funciona hoy?
* **Detección y Distinción Inteligente de Títulos de Capítulos en Modo Letra Grande (v1.5.0):**
  - **Detección Geométrica y Heurística:** El motor analiza el histograma de tamaños de fuente del PDF en `src/core/pdf-viewer.js` para clasificar títulos (`scale >= 1.35x` o patrones de capítulo) y subtítulos (`scale >= 1.15x`).
  - **Soporte de Títulos Propios sin la Palabra "Capítulo":** Para libros que titulan sus capítulos con nombres propios (ej: "La infancia minusválida") sin anteponer "Capítulo 1", el analizador semántico en `src/gui/text-mode-controller.js` identifica frases cortas en mayúscula sin puntuación terminal como encabezados de capítulo.
  - **Diseño Tipográfico Jerárquico:** Los títulos se formatean como `<h2 class="reading-chapter-title">` con tamaño aumentado (`1.42em` que escala dinámicamente con `A+`/`A-`), peso `800`, centrado elegante y línea divisoria inferior.
  - **Armonía de Colores por Preset:** Vinculación de `--reader-heading-color` adaptativo a cada modo (ámbar luminoso en Noche/OLED, tonos terrosos en Sepia/Cálido, negro puro en e-Ink).
  - **16/16 Tests Pasando al 100%.**
  - **Service Worker v1.3.7:** Caché actualizada para despliegue sin internet.

* **Barra Superior Simplificada y Espaciosa (v1.4.9):**
  - **Retiro de Botones Redundantes:** Se eliminaron los botones "Girar" (la rotación se realiza de forma nativa en el dispositivo) y "Pantalla" (el modo inmersivo se activa y desactiva directamente tocando el texto de la pantalla).
  - **Distribución Limpia y Amplia:** La cabecera aloja únicamente `[◀ Salir]` a la izquierda y `[👓 Texto / 📄 PDF]` junto a `[✨ Filtros]` a la derecha con `justify-content: space-between`.
  - **Cero Cortes y Máxima Accesibilidad:** El botón de filtros y el conmutador de texto son amplios, cómodos y 100% visibles en cualquier teléfono móvil sin necesidad de deslizamiento forzado.
  - **Service Worker v1.3.6:** Caché actualizada.
  - **14/14 Tests Pasando al 100%.**





* **Inmersión Total, Interfaz Translúcida y Gestos Táctiles Calibrados (v1.4.4):**
  - **Cero Bordes o Franjas Residuales:** Las barras superior e inferior son overlays flotantes con efecto translúcido de cristal (`backdrop-filter: blur(12px)`). Al ocultarse, el texto sube y ocupa el 100% de la pantalla sin dejar franjas, bordes o espacios vacíos del color de fondo.
  - **Desvanecimiento Automático del Botón Flotante (`fade-out`):** Al entrar en modo inmersivo, el botón flotante `[👁️ Mostrar Controles]` se desvanece suavemente tras 2.2 segundos para no tapar jamás el texto del libro.
  - **Discriminación de Deslizamiento (Scroll) vs Toque (Tap):** Si el usuario desliza el dedo por la pantalla para leer o hacer scroll, la aplicación NO interrumpe ni muestra los menús. Los menús solo reaparecen al dar un toque estático intencional.
  - **Auto-Ocultado Inteligente de Controles:** Al hacer reaparecer las barras, si el usuario no toca ningún control durante 3.8 segundos, las barras se ocultan solas automáticamente.
  - **Modularidad y Arquitectura Limpia:** Se creó `src/gui/immersion-controller.js` (153 líneas) manteniendo `app-controller.js` en 340 líneas y `reader.css` en 377 líneas (todos < 400 líneas).
  - **Service Worker v1.3.1:** Actualizada la caché con la nueva estructura modular.
  - **14/14 Tests Pasando al 100%.**

* **Corrección de Deformación de Texto, Inmersión Táctil y Tipografías en Vivo (v1.4.2):**
  - **Cero Deformación en Pantalla Vertical/Horizontal:** Se eliminó la restricción CSS que aplastaba el ancho del canvas mientras crecía la altura (`height: auto !important`, `flex-shrink: 0`). Ahora las letras mantienen su relación de aspecto original perfecta sin estirarse ni deformarse.
  - **Modo Inmersión / Lectura Limpia:** Al tocar cualquier parte de la lectura, todas las barras se ocultan instantáneamente, liberando el 100% de la pantalla para el libro (ideal en modo horizontal). Un toque en la pantalla o en el botón flotante `[👁️ Mostrar Controles]` restaura la interfaz.
  - **Cambio de Tipografía Real y Evidente:** Al activar `[👓 Modo Letra Grande]`, el texto extraído cambia inmediatamente de tamaño (`16px` a `42px`) y tipografía (📖 *Libro Clásico*, 🔤 *Moderna*, 👓 *Máxima Legibilidad*). En Modo PDF la barra conmuta automáticamente a controles de zoom sin confusión.
  - **Landscape Ultra-Compacto:** En modo horizontal las barras reducen su altura a menos de 40px para no tapar la lectura.
  - **Suite de Pruebas y Modularidad:** 14/14 pruebas superadas (`npm test`). Todos los archivos de código estrictamente por debajo de las 400 líneas.

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
