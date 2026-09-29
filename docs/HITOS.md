# Registro de Hitos Operativos y Versionado (SemVer)

> **¿QUÉ ES ESTE ARCHIVO Y CÓMO SE USA?:**  
> Este documento registra **hitos de negocio, entregas clave y el historial de versiones (SemVer: `MAJOR.MINOR.PATCH`)**.  
> **REGLA:**  
> - **MAJOR (X.0.0):** Cambios grandes, rompimiento de compatibilidad o lanzamiento formal.  
> - **MINOR (1.X.0):** Nuevas funcionalidades, pantallas o módulos agregados.  
> - **PATCH (1.0.X):** Correcciones de bugs, cálculos o estilos.

---

## 📦 Registro Detallado de Versiones (Changelog)

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
