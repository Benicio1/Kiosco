# proyectos/ — Hub de Proyectos del Workspace

> **PROPÓSITO DE ESTE MÓDULO:**  
> Este directorio centraliza y categoriza todos los proyectos, aplicaciones, extensiones y utilidades desarrolladas, organizándolos de manera modular según los estándares de [AGENTS.md](../AGENTS.md) y [docs/GUIA_DOCUMENTACION.md](../docs/GUIA_DOCUMENTACION.md).

---

## 📂 Organización por Categorías

| Categoría | Descripción | Proyectos Contenidos |
|---|---|---|
| [`web/`](./web/README.md) | Aplicaciones web, PWAs y servicios de backend | `gastro-stock-pwa` |
| [`desktop/`](./desktop/README.md) | Aplicaciones nativas de escritorio (Windows / .NET / C#) | `aura-canvas`, `winclean-lite` |
| [`extensiones/`](./extensiones/README.md) | Extensiones de navegador web (Manifest V3) | `brave-adblock` |
| [`gaming/`](./gaming/README.md) | Mods, plugins y lógica para videojuegos | `minecraft-mods` |
| [`drivers/`](./drivers/README.md) | Controladores de hardware y utilidades de sistema | `atheros-ar9271` |

---

## Reglas de Este Directorio

1. **Modularidad Estricta:** Cada subproyecto es autónomo y no debe depender de dependencias cruzadas no declaradas.
2. **Documentación Obligatoria:** Cada carpeta de proyecto debe contar con su propio `README.md` detallando su stack, propósito y forma de ejecución.
3. **Protección de Fuentes:** Las librerías transitorias (`node_modules`, cachés de compilación `.next`, `bin/`, `obj/`) se excluyen del control de versiones principal.
4. **Relación con el Contrato:**
   * [AGENTS.md](../AGENTS.md)
   * [docs/GUIA_DOCUMENTACION.md](../docs/GUIA_DOCUMENTACION.md)
