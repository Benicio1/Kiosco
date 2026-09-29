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
  - **Crunchyroll TV Hub (Intermediario) y Login Web Oficial:**
    - Se eliminó el iframe problemático que impedía el inicio de sesión (Cloudflare Turnstile y cookies cruzadas).
    - Se incorporó la pantalla intermedia **Crunchyroll TV Hub** con mini-catálogo nativo de animes (One Piece, Jujutsu Kaisen, Demon Slayer, Solo Leveling, etc.), filtros por género, visualización de trailers oficiales en TV y botón `[🌐 Abrir Crunchyroll Web (Con tu Cuenta)]`.
    - Al abrir la web oficial directamente en el navegador de la TV, el usuario puede iniciar sesión con su cuenta real y ver anime sin restricciones de DRM Widevine.
  - **Retorno Limpio al Menú y Fin de la "Ruleta de Banners":**
    - Se recompiló `tools/InputBridge.exe` eliminando el envío de `0x25` (flecha izquierda) y reemplazándolo por `WM_APPCOMMAND` (`APPCOMMAND_BROWSER_BACKWARD`) y `VK_BROWSER_BACK (0xA6)`.
    - En Crunchyroll Web (`tvClients.size === 0`), el botón **Atrás** del teléfono ordena al navegador retroceder en el historial de forma nativa hacia el Launcher sin activar el carrusel de banners de la web.
    - Dentro de la app (`tvClients.size > 0`), **Atrás** e **Inicio** cierran los modales y el TV Hub instantáneamente vía SSE sin interferir con el sistema operativo.
  - **Touchpad de Celular para Navegación:** El ratón táctil, scroll con 2 dedos y teclado remoto permiten interactuar con Crunchyroll Web con total fluidez.
  - **Verificación:** 8/8 pruebas superadas en `tv.test.mjs`.

### 2. ¿Qué se está haciendo ahora?
* Todo el código está implementado, probado y compilado.

### 3. ¿Qué está bloqueado / decisiones pendientes?
* Ningún bloqueo técnico.

### 4. Próximo paso inmediato
* El usuario ejecuta `CERRAR_SMART_TV.bat` y luego `INICIAR_TURBO.bat` para disfrutar de la nueva integración con Crunchyroll TV Hub, login web oficial y botón atrás sin interferencias.
