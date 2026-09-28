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
  - **Atrás e Inicio 100% Funcionales en Crunchyroll:** 
    - Corregido el problema de apertura de ventanas duplicadas ("host de página web"): el Launcher principal de la TV ya no se reemplaza en memoria, sino que abre Crunchyroll en una ventana de aplicación dedicada maximizada.
    - Al presionar **Inicio (Home)** o **Cerrar App**: se envía `Alt + F4` y se devuelve el foco inmediatamente al Smart TV Launcher a pantalla completa sin pestañas ni barras de direcciones.
    - Al presionar **Atrás (Back)**: se inyecta la tecla de hardware nativa `VK_BROWSER_BACK (0xA6)` y `Alt + Left`, permitiendo retroceder en el historial de navegación de Crunchyroll (de episodios a series, de series al catálogo).
  - **Barra de Navegación Rápida en el Touchpad Móvil:** Agregados botones accesibles en la pestaña de touchpad (`[↩️ Atrás]`, `[🏠 Menú TV]`, `[✖️ Cerrar App]`) para no tener que cambiar de pestaña para regresar o salir de Crunchyroll.
  - **D-Pad 1 a 1 sin Saltos Dobles:** Navegación secuencial exacta sin saltar Crunchyroll.
  - **Touchpad con Deslizamiento de 2 Dedos (Estilo Netbook):** Scroll suave con dos dedos y botones de página.
  - **InputBridge Nativo Windows Actualizado:** Recompilado con soporte `user32.dll` (`keybd_event`, `SetForegroundWindow`, `EnumWindows`).
  - **Verificación:** 8/8 pruebas superadas en `tv.test.mjs`.

### 2. ¿Qué se está haciendo ahora?
* Todo el código está implementado, probado y compilado.

### 3. ¿Qué está bloqueado / decisiones pendientes?
* Ningún bloqueo técnico.

### 4. Próximo paso inmediato
* El usuario ejecuta `CERRAR_SMART_TV.bat` y luego `INICIAR_TURBO.bat` para probar el control de Atrás e Inicio en Crunchyroll.
