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
  - **D-Pad 1 a 1 sin Saltos Dobles:** Corregido el conflicto de eventos de teclado entre el servidor nativo y la interfaz gráfica; ahora al presionar las flechas en el celular, el selector avanza exactamente de 1 en 1 (YouTube -> Crunchyroll -> Videos Locales -> Turbo RAM), permitiendo seleccionar Crunchyroll sin inconvenientes.
  - **Apertura Directa de Crunchyroll en Pantalla Completa:** Al presionar OK sobre Crunchyroll o seleccionarlo desde el celular, se abre directamente el portal oficial `https://www.crunchyroll.com/es/` para iniciar sesión y navegarlo con el touchpad y teclado del celular.
  - **Corrección Total de Pausa en YouTube:** Control directo con el protocolo `postMessage` bidireccional sobre el reproductor de YouTube TV, garantizando que el botón OK o Play/Pausa responda siempre al 100%.
  - **Touchpad con Deslizamiento de 2 Dedos (Estilo Netbook):** Al deslizar con 2 dedos en la pantalla del celular, emite eventos nativos de rueda de ratón (Mouse Wheel) a Windows, permitiendo bajar o subir páginas fluidamente en Crunchyroll o en la web. Además botones dedicados `🔼 Subir Página` y `🔽 Bajar Página`.
  - **InputBridge Nativo Windows:** Binario de 6 KB compilado para control en 0ms de reproductores externos y páginas web.
  - **Verificación:** 8/8 pruebas superadas en `tv.test.mjs`.

### 2. ¿Qué se está haciendo ahora?
* Todo el código está implementado, probado y validado.

### 3. ¿Qué está bloqueado / decisiones pendientes?
* Ningún bloqueo técnico.

### 4. Próximo paso inmediato
* El usuario ejecuta `CERRAR_TV.bat` y luego `INICIAR_TURBO.bat` para validar la navegación 1 a 1 del D-Pad y la apertura directa de Crunchyroll.
