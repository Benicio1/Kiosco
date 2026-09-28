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
  - **YouTube TV Nativo Integrado (Leanback UI Simulator):** Al presionar YouTube o presionar OK desde el celular, no abre ventanas externas de Chrome ni popups bloqueados. Se abre la interfaz Smart TV de YouTube dentro de la misma aplicación, con categorías (Tendencias, Música, Noticias, Cumbia, Trap, Fútbol), buscador directo y reproductor a pantalla completa.
  - **Control Total desde el Celular:** Play/Pausa (OK), retroceso/avance de 10s (◀ / ▶), control de volumen y botón Atrás (↩️) para regresar al catálogo de videos sin usar ratón.
  - **Optimizado para 4 GB RAM en Windows 11:** Arquitectura sin frameworks pesados (< 40 MB de RAM), con monitor en vivo de consumo en pantalla y aceleración por GPU.
  - **Lanzadores Turbo (`INICIAR_TURBO.bat`):** Script que limpia la memoria de Windows y lanza la TV en modo Kiosco a pantalla completa.
  - **Verificación:** 8/8 pruebas superadas en `tv.test.mjs`.

### 2. ¿Qué se está haciendo ahora?
* Listo para que el usuario pruebe YouTube TV integrado y lo maneje 100% desde el celular.

### 3. ¿Qué está bloqueado / decisiones pendientes?
* Ningún bloqueo técnico.

### 4. Próximo paso inmediato
* El usuario abre `INICIAR_TURBO.bat` (o `INICIAR_TV.bat`), entra a YouTube TV con el control y reproduce cualquier video directamente en pantalla grande.
