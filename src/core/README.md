# src/core/ — Núcleo de Dominio del Kiosco

> **PROPÓSITO:**  
> Contiene la lógica pura de negocio, reglas de dominio y entidades de datos del organizador de faltantes de kiosco, desacopladas de cualquier interfaz gráfica, framework o almacenamiento físico.

---

### 1. ¿Qué entra aquí?
* Clases y modelos de entidad pura (`Item.mjs`).
* Gestores de lista y agregados de reposición (`KioscoList.mjs`).
* Catálogo de productos iniciales solicitados por el usuario (`InitialData.mjs`).
* Validaciones de reglas de negocio (nombres obligatorios, cantidades no negativas, estado de faltante).

### 2. ¿Qué NO entra aquí?
* ❌ Elementos del DOM (`document`, `window`, selectores HTML, eventos click).
* ❌ Llamadas directas a `localStorage` o APIs del navegador (eso reside en `src/adapters/`).
* ❌ Precios (por requerimiento explícito del usuario no se manejan precios).

### 3. Archivos Principales
* `Item.mjs`: Entidad inmutable/controlada para cada ítem a reponer.
* `KioscoList.mjs`: Manejo de colecciones, filtros, tachado, métricas y formato de exportación a WhatsApp.
* `InitialData.mjs`: 9 productos específicos solicitados (Promo de panchos, Sanguchitos de miga, Guaymallén, Fulbito, Baggio multifruta, Picodulces, Chupetín con chicle, Flynn Paff, Palitos de la selva).

### 4. Responsable y Normativa
* Responsable: MÖLDEA Core Architecture / Antigravity
* Referencias canónicas: [AGENTS.md](../../AGENTS.md) y [GUIA_DOCUMENTACION.md](../../docs/GUIA_DOCUMENTACION.md).
