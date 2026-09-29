# Registro de Hitos Operativos y Versionado (SemVer)

> **¿QUÉ ES ESTE ARCHIVO Y CÓMO SE USA?:**  
> Este documento registra **hitos de negocio, entregas clave y el historial de versiones (SemVer: `MAJOR.MINOR.PATCH`)**.  
> **REGLA:**  
> - **MAJOR (X.0.0):** Cambios grandes, rompimiento de compatibilidad o lanzamiento formal.  
> - **MINOR (1.X.0):** Nuevas funcionalidades, pantallas o módulos agregados.  
> - **PATCH (1.0.X):** Correcciones de bugs, cálculos o estilos.

---

## 📦 Registro Detallado de Versiones (Changelog)

### [1.6.0] — 2026-09-29 *(Apertura y Reanudación Directa de Libros con Caché IndexedDB en 1 Toque)*
* **🚀 Agregado:**
  * Módulo de almacenamiento local binario `src/core/book-cache.js` basado en IndexedDB con soporte offline y fallback seguro.
  * Al seleccionar o abrir un libro, el archivo PDF se almacena automáticamente en el dispositivo.
  * Botón "Continuar 📖" abre inmediatamente el documento y salta a la última página leída, sin pedir al usuario volver a buscar el archivo en el explorador de su celular.
  * Limpieza automática de la memoria IndexedDB al eliminar un libro de la lista reciente.
  * Refactorización limpia de `src/gui/app-controller.js` manteniéndolo en 374 líneas (< 400 líneas).
  * Suite ampliada a 18 pruebas automatizadas (`npm test`) superadas al 100%.

### [1.5.1] — 2026-09-29 *(Párrafos Continuos sin Pausas Falsas y Espaciado Natural)*
* **🐛 Corregido:**
  * Eliminada la separación arbitraria de líneas en párrafos independientes dentro del cuerpo de texto (`src/core/pdf-viewer.js`).
  * Las líneas continuas que no terminan en punto de cierre (`[.?!…»"”]`) ahora se fusionan de forma continua y fluida en un único párrafo.
  * Reconstrucción y unión de palabras cortadas por guion tipográfico al final de línea (`palabra-` + `siguiente` -> `palabrasiguiente`).
  * Reducción del espaciado vertical entre párrafos (`margin-bottom: 0.85em;`) para una lectura armónica sin pausas forzadas.
  * Fallback de unión de líneas en `src/gui/text-mode-controller.js` (`mergeRawLines`).
  * Suite ampliada a 17 pruebas automatizadas (`npm test`) superadas al 100%.

### [1.5.0] — 2026-09-29 *(Detección Inteligente de Títulos de Capítulos en Modo Letra Grande y Simplificación de Cabecera)*
* **🚀 Agregado:**
  * Algoritmo de detección de títulos de capítulos y secciones en el motor PDF (`src/core/pdf-viewer.js`) mediante agrupamiento de escala tipográfica (histograma de fuentes del documento).
  * Analizador semántico y heurístico en `src/gui/text-mode-controller.js` para libros con títulos de capítulos propios (ej: "La infancia minusválida") sin la palabra "Capítulo".
  * Renderizado tipográfico jerárquico: títulos con tamaño proporcional `1.42em` (escalable con `A+`/`A-`), peso `800`, centrado y línea divisoria inferior color ámbar.
  * Color de título adaptativo por tema (`--reader-heading-color`) en `src/core/filter-engine.js`.
  * Cabecera simplificada a los botones esenciales: `[◀ Salir]`, `[👓 Texto / 📄 PDF]` y `[✨ Filtros]`.
  * Suite ampliada a 16 pruebas automatizadas (`npm test`) superadas al 100%.

### [1.4.2] — 2026-09-29 *(Corrección de Deformación de Canvas, Modo Inmersión Táctil y Tipografías Dinámicas)*
* **🐛 Corregido:**
  * Eliminación de la distorsión vertical en el modo PDF eliminando el conflicto de `max-width: 100%` con alturas fijas de canvas. Ahora el aspect-ratio es 100% fiel y natural en horizontal y vertical.
  * Solución a la interfaz fija en landscape: al tocar cualquier parte de la lectura, todas las barras se ocultan inmediatamente liberando la pantalla entera para el texto. Un toque o el botón flotante `[👁️ Mostrar Controles]` las devuelve.
  * Separación contextual de barras de control: cuando se está en Modo Texto se muestra el selector de tipografías dinámicas que cambia la fuente al instante en pantalla; en Modo PDF se muestran controles de Zoom y Ajuste al Ancho.
  * Media queries para pantallas horizontales con barras compactas (< 42px).

### [1.4.1] — 2026-09-29 *(Mejora de Accesibilidad: Letra Grande para Adultos, Tipografías y Navegación Intuitiva)*
* **🚀 Agregado:**
  * Modo Letra Grande Adaptable (Reflow) con extracción de texto de la página y tipografía fluida (`src/gui/text-mode-controller.js`).
  * Botones grandes y directos `[A-]` y `[A+]` para aumentar el tamaño de letra (hasta 42px) o zoom del PDF.
  * Selector de tipografía cómoda para la vista (📖 Libro Clásico, 🔤 Moderna y Limpia, 👓 Máxima Legibilidad).
  * Rediseño intuitivo para adultos: botones claros con texto en español (`[◀ Salir]`, `[◀ Anterior]`, `[Siguiente ▶]`, `[👓 Letra Grande]`).
  * Botón de prueba inmediata `[📘 Probar con el Libro de Ejemplo]` en la pantalla de inicio.
  * Desacoplamiento modular de controladores (`filter-modal-controller.js` y `reader.css`) garantizando cero archivos que superen 400 líneas.
  * Despliegue en repositorio independiente `https://benicio1.github.io/lector/`.
  * Suite de 14 pruebas automatizadas (`npm test`) superadas al 100%.

### [1.4.0] — 2026-09-29 *(Nuevo Proyecto: Lector Confort PDF para Teléfono Móvil con Filtros Antifatiga Visual)*
* **🚀 Agregado:**
  * Nuevo proyecto autónomo [`proyectos/web/lector-pdf-pwa/`](../proyectos/web/lector-pdf-pwa/README.md).
  * Selector nativo de archivos PDF para teléfonos inteligentes (`<input type="file" accept="application/pdf">` táctil y drag & drop).
  * Motor de confort visual con 6 modos predefinidos: Normal (Día), Luz Cálida (filtro anti-azul 2700K), Sepia (papel de libro impreso), Modo Noche (carbón antirreflejo), OLED (negro puro #000000 con apagado de píxeles) y e-Ink (tinta electrónica en escala de grises).
  * Controles deslizantes de calibración fina para Brillo (25% a 130%), Calidez/Ámbar (0% a 100%) y Contraste de lectura (70% a 140%).
  * Visor inmersivo para celular con PDF.js empaquetado 100% offline, soporte de zoom táctil, ajuste automático al ancho del teléfono, gestos táctiles swipe y ocultamiento automático de barras de menú por toque en pantalla.
  * Biblioteca local con memoria automática de progreso y porcentaje de lectura por archivo en `localStorage`.
  * PWA instalable con manifest, Service Worker Cache-First e iconos adaptativos HD generados con .NET.
  * Servidor local Wi-Fi con detección de IP para abrir en el celular y lanzador rápido `INICIAR_LECTOR.bat`.
  * Suite de 11 pruebas automatizadas (`npm test` con Node.js Test Runner) superadas al 100%.

### [1.3.0] — 2026-09-26 *(Formato PWA Completo: Cartel de Instalación Superior e Iconos Nativos)*
* **🚀 Agregado:**
  * Soporte PWA (Progressive Web App) completo con `manifest.json` y `sw.js` (Service Worker para caché y uso offline).
  * Iconos nativos de alta resolución (192x192, 512x512, apple-touch-icon 180x180 y versiones maskable) generados en `icons/` y `src/gui/icons/`.
  * Cartel flotante superior interactivo ("📲 Instalar Kiosco App") con botón "Instalar" que dispara el instalador nativo del celular (Chrome/Android) o abre la guía para iPhone (Safari).
  * Modal visual instructivo con paso a paso detallado para instalar en Android y en iPhone.
  * Módulo dedicado `src/gui/pwa.js` y estilos `src/gui/pwa.css` respetando el límite estricto de 400 líneas.

### [1.2.0] — 2026-09-26 *(Modo Autónomo Móvil: Uso en Escuela sin Computadora ni Red Local)*
* **🚀 Agregado:**
  * Generador de compilación para distribución en `dist/` (`herramientas/build_dist.mjs` y `npm run build`).
  * Generación de archivo único ultra-portable **`dist/kiosco_app_celular.html`** (embebido de HTML+CSS+JS) para enviar por WhatsApp o guardar en el teléfono y usar sin internet, sin PC y sin depender de ninguna red Wi-Fi.
  * Distribución estática lista para subir a Netlify Drop, GitHub Pages, Vercel o Cloudflare Pages.
  * Guía operativa en [`docs/guias/USAR_EN_LA_ESCUELA_SIN_PC.md`](guias/USAR_EN_LA_ESCUELA_SIN_PC.md).

### [1.1.2] — 2026-09-26 *(Compatibilidad Universal sin Recargas y Simplificación de Cantidad)*
* **🐛 Corregido:**
  * Eliminación de cualquier recarga o reinicio de página al agregar productos con `onsubmit="return false"` y handlers directos.
  * Remoción de los botones `+`/`-` redundantes en la caja de añadir para usar el selector numérico nativo limpio con sus flechas.
  * Migración de `app.js` a arquitectura universal para que funcione tanto abriendo con `SERVER.bat` (HTTP) como haciendo doble clic directo en `index.html` (`file:///`).
  * Los botones superiores de stock ("Faltan Reponer", "En Stock / Listos", "Todos") actualizan los contadores y la vista al instante.

### [1.1.1] — 2026-09-26 *(Mejora de UX Móvil: Adición Rápida en Pantalla y Botones de Control Interactivos)*
* **🚀 Agregado:**
  * Caja directa de adición rápida visible en la pantalla principal ("➕ Añadir lo que falta") con nombre, selector numérico `+`/`-`, unidad y botón destacado.
  * Botones de "Control de Reposición y Stock" superiores ahora 100% interactivos con un toque para alternar entre "Faltan Reponer", "En Stock / Listos" y "Todos".
  * Sugerencia automática de adición con un tap cuando una búsqueda no encuentra coincidencias.
  * Efecto visual de resaltado (`glow`) y vibración háptica al agregar cualquier nuevo producto.
  * Modularización de componentes (`uiRenderer.js`, `modals.css`).

### [1.1.0] — 2026-09-26 *(Organizador Móvil de Faltantes de Kiosco)*
* **🚀 Agregado:**
  * Aplicación Web Mobile-First / PWA para gestión rápida de faltantes y reposición en el teléfono.
  * Catálogo base inicial con los 9 ítems solicitados (panchos, sanguchitos, Guaymallén, Fulbito, Baggio, picodulces, chupetín, Flynn Paff, palitos de la selva) sin precios.
  * Sistema de tachado/destacado con un toque, control de cantidades (`+`/`-`), agregador y editor de ítems.
  * Buscador en tiempo real insensible a tildes y filtros por estado ("Faltan", "Todos", "En stock").
  * Generador de mensaje formateado y exportador para WhatsApp.
  * Servidor local con visualización de IP de red Wi-Fi y código QR para abrir instantáneamente en el smartphone.
  * Suite de pruebas automatizadas con Node.js Test Runner (17 pruebas al 100%).

### [1.0.0] — 2026-09-26 *(Inicialización)*
* **🚀 Agregado:**
  * Estructura inicial del proyecto generada con MÖLDEA Scaffolder.
  * Contrato canónico de trabajo en `AGENTS.md` con matriz de riesgo de 4 zonas.
  * Suite de documentación viva: `docs/ESTADO_ACTUAL.md`, `docs/PLAN.md`, `docs/bugs/BUGS.md`.
  * Scripts de automatización en Git local (`COMMITEAR.bat` / `COMMITEAR.ps1`).

---

## 💡 Ejemplo de Referencia (Cómo documentar versiones futuras de forma simple y concisa):
```markdown
### [1.5.0] — 2026-11-15
* **🚀 Agregado:**
  * Exportación de presupuestos a PDF y mensaje directo de WhatsApp.
  * Selector inteligente de tipo de boquilla (0.4mm, 0.6mm, 0.8mm).
* **🐛 Corregido:**
  * Corrección de redondeo en el cálculo de consumo eléctrico por hora.
  * Solución a bug de carga lenta al procesar mallas 3D mayores a 50MB.
* **⚡ Optimización:**
  * Reducción de tiempo de renderizado 3D en un 40%.
```

---

## 📅 Bitácora Histórica de Hitos Humanos y de Negocio

| Fecha | Tipo | Resumen del Hito / Novedades | Responsable |
|---|---|---|---|
| 2026-09-26 | INICIO | Inicialización estructurada del proyecto con MÖLDEA Scaffolder | MÖLDEAstudio |
