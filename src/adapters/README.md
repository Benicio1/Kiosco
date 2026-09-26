# src/adapters/ — Adaptadores de Infraestructura y Dispositivos

> **PROPÓSITO:**  
> Implementa las conexiones externas del sistema con el entorno de ejecución: almacenamiento local del navegador (`localStorage`), APIs nativas del teléfono (Web Share API, Portapapeles, WhatsApp) y exportación de datos.

---

### 1. ¿Qué entra aquí?
* `StorageAdapter.mjs`: Lectura y escritura en `localStorage` con tolerancia a fallos y fallback en memoria.
* `ShareAdapter.mjs`: Integración directa con WhatsApp Web/App y el portapapeles del smartphone.
* Adaptadores para importación y descarga de respaldos en JSON.

### 2. ¿Qué NO entra aquí?
* ❌ Lógica de negocio (eso reside en `src/core/`).
* ❌ Renderizado de componentes visuales o estilos CSS (eso reside en `src/gui/`).

### 3. Responsable y Normativa
* Responsable: MÖLDEA Core Architecture / Antigravity
* Referencias canónicas: [AGENTS.md](../../AGENTS.md) y [GUIA_DOCUMENTACION.md](../../docs/GUIA_DOCUMENTACION.md).
