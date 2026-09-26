# src/gui/ — Interfaz Móvil y Componentes Visuales

> **PROPÓSITO:**  
> Capa de presentación visual táctil (Mobile-First) diseñada específicamente para smartphones, con compatibilidad PWA, microinteracciones táctiles y acceso en red local mediante QR.

---

### 1. ¿Qué entra aquí?
* `index.html`: Estructura accesible y responsive orientada a celulares.
* `styles.css`: Estilos visuales optimizados para pantallas táctiles con safe-areas.
* `app.js`: Controlador que orquesta la UI, los eventos de usuario y el Core.
* `qr.js`: Módulo visual para generar el enlace rápido y QR hacia el celular.
* `manifest.json` & `sw.js`: Capacidades PWA y funcionamiento sin conexión.

### 2. ¿Qué NO entra aquí?
* ❌ Lógica de negocio dura o validaciones centrales (eso reside en `src/core/`).
* ❌ Código servidor o scripts de build.

### 3. Responsable y Normativa
* Responsable: MÖLDEA Presentation Layer / Antigravity
* Referencias canónicas: [AGENTS.md](../../AGENTS.md) y [GUIA_DOCUMENTACION.md](../../docs/GUIA_DOCUMENTACION.md).
