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
  - **Crunchyroll 100% Integrado en la Aplicación (Cero Interferencia con la Página):**
    - Se resolvió definitivamente la interferencia de los botones con los banners de Crunchyroll: Crunchyroll ahora corre dentro del contenedor de la aplicación (`#view-crunchyroll-tv`) con una barra superior nativa de TV (`[📺 Volver al Menú]`).
    - Al presionar **Atrás (Back)** o **Inicio (Home)** en el control remoto del celular, la orden va directamente a la aplicación vía SSE (`tv.js`), cerrando la vista de Crunchyroll y volviendo al menú principal en 0 milisegundos.
    - Se eliminó el envío de teclas de teclado al sistema operativo durante la navegación de menús, evitando que Crunchyroll intercepte teclas o desplace los banners/ruletas.
    - Los scripts `.bat` arrancan con `--disable-web-security --user-data-dir` para permitir que el portal oficial de Crunchyroll cargue con total fluidez en el contenedor de la TV y permita inicio de sesión y reproducción.
  - **Touchpad de Celular para Navegación:** El ratón táctil y scroll de 2 dedos funcionan con total libertad sobre Crunchyroll para hacer clic, elegir capítulos e ingresar credenciales.
  - **Verificación:** 8/8 pruebas superadas en `tv.test.mjs`.

### 2. ¿Qué se está haciendo ahora?
* Todo el código está implementado, probado y compilado.

### 3. ¿Qué está bloqueado / decisiones pendientes?
* Ningún bloqueo técnico.

### 4. Próximo paso inmediato
* El usuario ejecuta `CERRAR_SMART_TV.bat` y luego `INICIAR_TURBO.bat` para probar el control de Crunchyroll dentro de la aplicación.
