# tests/ — Banco de Pruebas Automatizadas del Kiosco

> **PROPÓSITO:**  
> Garantiza mediante pruebas continuas unitarias y de integración que los requerimientos funcionales del organizador de faltantes del kiosco se cumplan rigurosamente, previniendo regresiones y validando la modularidad.

---

### 1. ¿Qué entra aquí?
* `kiosco.test.mjs`: Suite de pruebas con Node.js Test Runner nativo (`node --test`).
* Validaciones de catálogo (exactitud de los 9 productos solicitados, ausencia de precios).
* Pruebas de dominio (tachar, destachar, incrementar, decrementar cantidades, validaciones).
* Pruebas de adaptadores (persistencia y compartir).
* Pruebas estáticas de arquitectura (regla inmutable de menos de 400 líneas por archivo en `src/`).

### 2. ¿Cómo se ejecutan las pruebas?
```bash
node --test tests/kiosco.test.mjs
```

### 3. Responsable y Normativa
* Responsable: MÖLDEA Quality Assurance / Antigravity
* Referencias canónicas: [AGENTS.md](../AGENTS.md) y [GUIA_DOCUMENTACION.md](../docs/GUIA_DOCUMENTACION.md).
