# AGENTS.md — Contrato Canónico de Trabajo (MÖLDEA v1.0.0)

> **PROPÓSITO DE ESTE DOCUMENTO:**  
> Este archivo es el **contrato operativo único** del proyecto. Define los límites de autonomía, reglas de arquitectura y estándares de calidad para cualquier colaborador (humano o agente de IA: Antigravity, Claude, Codex).  
> **REGLA:** Todo agente de IA debe leer este archivo antes de comenzar a trabajar.

---

## 1. Contexto Rápido
* **Proyecto:** Antigravity full organizado
* **Versión:** v1.0.0 (SemVer: MAJOR.MINOR.PATCH)
* **Tipo:** web
* **Preset:** [ESTANDAR]
* **Stack:** A definir según requerimientos (ver docs/PLAN.md)
* **Autor / Responsable:** MÖLDEAstudio
* **Fecha de Creación:** 2026-09-26

---

## 2. Matriz de Autonomía y Riesgo

| Zona | Tipo de Acción | Regla Operativa |
|---|---|---|
| 🟢 **Verde** | Leer archivos, investigar, correr tests, cambios reversibles dentro de `src/` y `docs/` | **Avanzar sin preguntar** |
| 🟡 **Amarilla** | Crear archivos nuevos fuera de `src/`, refactors estructurales, cambiar dependencias | **Avanzar y reportar en el resumen** |
| 🔴 **Roja** | Borrar datos, sobreescribir trabajo previo, publicar, desplegar, enviar mensajes externos | **Pedir confirmación SIEMPRE** |
| ⛔ **Detención** | Falta una decisión de negocio/diseño que cambia el resultado esperado | **Parar y consultar** |

---

## 3. Reglas Inmutables de Desarrollo (Mandatorias para toda IA y Colaborador)

1. **Definición de Stack (Si está pendiente):**
   * El Stack figura como **Por definir**. Tu primera tarea como IA es leer la visión y requerimientos en `docs/PLAN.md` y proponer la tecnología óptima (lenguaje, librerías, dependencias) antes de generar código.
2. **Regla Primordial de Modularidad (Límite Máximo 400 Líneas por Archivo):**
   * Ningún archivo de código (`.js`, `.mjs`, `.ts`, `.py`, etc.) debe superar las **400 líneas de código**.
   * **Excepción Justificada:** Si excepcionalmente las supera (ej: 410-420 líneas), DEBE existir un motivo técnico válido y explícito documentado en la cabecera del archivo: que resuelva una sola función o algoritmo indivisible altamente cohesivo y que fragmentarlo aumente la complejidad accidental y el acoplamiento más que mantenerlo unificado.
   * Fuera de esta excepción justificada, todo módulo que crezca debe modularizarse tempranamente extrayendo responsabilidades a submódulos, adaptadores o helpers.
3. **Testeo y Verificación Operativa (Prohibido dar por hecho solo por codear):**
   * Prohibido asumir que una funcionalidad funciona solo por haber escrito código o porque no arrojó errores de sintaxis en el editor.
   * La IA DEBE probar activamente lo que programó (correr tests, validar endpoints HTTP, probar comandos o flujos) antes de dar la tarea por concluida.
   * Regla de oro: **Terminado significa estrictamente PROBADO + DOCUMENTADO**.
4. **Versionado Dual (Git + Web / Bitácora Visible de Avances):**
   * Todo proyecto comienza en `1.0.0` y escala con SemVer:
     * **MAJOR** (1.0.0 ➔ 2.0.0): cambio grande, ruptura de compatibilidad o rediseño estructural.
     * **MINOR** (1.0.0 ➔ 1.1.0): funcionalidad, pantalla, módulo o capacidad nueva compatible.
     * **PATCH** (1.0.0 ➔ 1.0.1): corrección pequeña, bug, texto, estilo o ajuste sin cambiar el contrato.
   * El avance no solo vive en commits de Git, sino que debe registrarse de forma visible para el usuario: impactar `.scaffold.json`, `package.json`, documentar cada versión formal en `docs/HITOS.md` y detallar los avances del turno en `docs/ESTADO_ACTUAL.md`, para que el usuario conozca el progreso exacto en la Web sin necesidad de abrir la consola de Git.
5. **Memoria In-Repo contra la Pérdida de Contexto del LLM:**
   * Las IAs sufren amnesia de contexto entre sesiones o por compactación de ventana. La única defensa es la memoria persistente en el repositorio:
   * **Al iniciar una sesión:** Leer obligatoriamente `docs/ESTADO_ACTUAL.md` (parte de turno) y los últimos 10 bugs de `docs/bugs/BUGS.md`. NO releer archivos innecesarios.
   * **Al finalizar una tarea:** Es obligatorio actualizar `docs/ESTADO_ACTUAL.md` reflejando qué se hizo, qué funciona y próximos pasos.
   * Si se resolvió una incidencia técnica no trivial, registrarla en `docs/bugs/BUGS.md` con formato canónico: **Síntoma | Causa | Solución**.
   * Si la IA no documenta, la siguiente sesión arrancará a ciegas y romperá decisiones previas.
6. **Modularidad y Límites de Capa:**
   * Respetar las reglas de cada carpeta declaradas en sus respectivos `README.md`.
   * Si no sabes dónde va un archivo nuevo, **pregunta antes de inventar carpetas**.
7. **Git Local-First:**
   * Commits claros en formato Conventional Commits (`feat:`, `fix:`, `refactor:`, `docs:`, `chore:`).
   * No asumir que hay conexión a servidores remotos; el repositorio local es soberano.
8. **Separación de Código Fuente (`src/`) y Distribución Protegida / Ofuscada (`dist/`):**
   * Todo el código legible y modular se escribe y edita **exclusivamente en `src/`**.
   * La carpeta `dist/` es la salida de distribución generada por el proceso de build, empaquetado u ofuscación comercial.
   * ⛔ **Zona Roja 🔴:** Las IAs y colaboradores tienen **estrictamente prohibido editar manualmente dentro de `dist/`**. Cualquier cambio debe realizarse en `src/` y regenerar la distribución mediante el proceso de build.
9. **Arquitectura y Desacoplamiento (Minimizar el Radio de Impacto / Blast Radius):**
   * **Aislamiento de Capas:** El dominio central (`src/core/` o modelos) nunca debe importar ni depender de frameworks de presentación (`src/gui/`), transportes HTTP o bases de datos (Arquitectura Hexagonal / Puertos y Adaptadores).
   * **Objetos de Parámetros:** Prohibido definir funciones con 4 o más argumentos posicionales sueltos (`f(a, b, c, d)` - *Conascencia de Posición*). Usar siempre objetos de opciones destructurados (`f({ targetDir, options, preset })`).
   * **Ley de Demeter (Mínimo Conocimiento):** Prohibido el encadenamiento transitivo profundo (`a.getB().getC().execute()`). Cada módulo interactúa únicamente con sus colaboradores directos.
   * **Erradicación de la Obsesión por Primitivos:** Encapsular estados y reglas de negocio críticas en Objetos de Valor inmutables (*Value Objects*) con validación intrínseca.
   * **Cero Filtraciones de Infraestructura en Dominio:** Los ORMs, queries SQL crudas, llamadas HTTP externas y lectura de archivos viven en adaptadores/infraestructura, exponiendo interfaces limpias al dominio.
10. **Sugerencia Proactiva de Skills del Ecosistema MÖLDEA (MÖLDEA Studio / `MOLDEA.exe` / `moldea skills`):**
   * Cuando el proyecto entre en una fase temática específica (UI/UX, Base de Datos/Backend, Seguridad/Tokens, SEO/GEO, Testing, Desktop), el agente **debe sugerir proactivamente al usuario revisar el ejecutable / MÖLDEA Studio (`SERVER.bat` o `MOLDEA.exe`) o correr `moldea skills recomendar`** para activar las capacidades curadas y verificadas con SHA-256 (ej: `ui-ux-pro-max` en diseño, `trufflehog`/`semgrep` en seguridad, `geo-engine-optimizer` en SEO/GEO, `firecrawl`/`crawl4ai` en extracción).
11. **Documentación de carpetas nuevas:**
   * Antes de crear una carpeta, revisá el `README.md` de su carpeta padre y ubicá allí la regla específica del módulo.
   * Toda carpeta nueva que contenga código, documentación, datos o assets debe incluir su propio `README.md` en el mismo cambio.
   * Ese README debe explicar: propósito, qué entra, qué no entra, archivos principales, responsable y enlaces a `AGENTS.md` y `docs/GUIA_DOCUMENTACION.md`.
   * No dupliques reglas entre READMEs: el contrato general vive en `AGENTS.md`; cada README solo agrega reglas de su módulo.

---

## 4. Protocolo de Diagnóstico y Onboarding Inicial (Primera Sesión)

Cuando una IA inicia por primera vez en un proyecto recién creado (o si la sección 2 de `docs/PLAN.md` está incompleta), la IA **DEBE realizar una breve entrevista diagnóstica guiada al usuario** antes de escribir código:

1. 🎯 **Tipo de Producto / Software:**
   * ¿Qué estamos construyendo? (Web App / Landing Page / Sistema de Gestión / Software de Escritorio Tradicional / Script / Juego / API Backend / Submódulo).
2. 📦 **Formato de Entrega y Portabilidad:**
   * ¿Cómo se distribuirá o ejecutará? (Web en la nube / **Archivo Portable `.exe` sin instalación** / Instalador clásico de Windows / Script local / Submódulo integrable en otro software como Fan 3D).
3. 🔒 **Estrategia de Protección de Propiedad Intelectual:**
   * ¿Código abierto / uso interno (sin ofuscación)?
   * ¿Fórmulas propietarias que deben protegerse (ofuscación en `dist/`, backend privado en Cloudflare o binario `.exe` cerrado)?
   * ¿O es una etapa prototipo donde NO se requiere ofuscar todavía, pero sí dejar la arquitectura modular lista para producción?
4. 🚀 **Propuesta de Stack y Arquitectura:**
   * Proponer el stack técnico óptimo basado en las respuestas y en la experiencia previa del equipo.

**REGLA DE PERSISTENCIA OBLIGATORIA:**  
La IA debe **guardar los acuerdos en `docs/PLAN.md`** bajo la sección `## 🧭 2. Diagnóstico de Destino, Integración y Protección`. Así, en futuras sesiones o cuando trabaje otra IA diferente (Claude, Antigravity, Codex), el contexto queda sellado y no se vuelve a preguntar.

---

## 5. Grafo de Contexto (Understand-Anything)

Este proyecto mantiene un grafo de conocimiento del código (`.ua/knowledge-graph.json`, no versionado en git) generado con [Understand-Anything](https://github.com/Egonex-AI/Understand-Anything). Es complementario a `docs/ESTADO_ACTUAL.md`: el parte de turno dice qué se está haciendo, el grafo muestra cómo está armado el código.

**Regla operativa para cualquier IA (Claude, Codex, Antigravity/Gemini, OpenCode) o humano:**
1. Antes de explorar el código a mano, correr `node src/index.mjs revisar` (o `moldea revisar`). Si reporta `[GRAFO DESACTUALIZADO]`, es señal de regenerarlo antes de seguir.
2. **Cuándo se considera viejo** (cualquiera de estas condiciones):
   * Nunca se generó.
   * Pasaron más de **14 días** desde la última generación.
   * Hubo más de **15 commits** desde la última generación.
   * Vos (IA) notás un cambio estructural grande (carpeta nueva en `src/`, módulo eliminado, refactor mayor) aunque no se cumplan los umbrales de arriba — usá criterio, no solo el contador.
3. **Cómo regenerarlo:** correr el comando de tu asistente (`/understand` en Claude Code, Codex, Gemini CLI/Antigravity u OpenCode — Understand-Anything es agnóstico de cuál IA lo invoca). Si la herramienta no está instalada, ver `docs/guias/GRAFO_CONTEXTO.md`.
4. **Después de regenerarlo:** correr `node src/index.mjs grafo marcar` para sellar la fecha y el commit actual en `.scaffold.json`. Sin este paso, `revisar` lo va a seguir marcando como desactualizado.

---

## 6. Reglas para Aprendices y Nuevos Colaboradores

1. Antes de escribir código: lee `docs/bugs/BUGS.md` para no repetir errores ya resueltos.
2. Si tienes dudas de arquitectura: consulta antes de improvisar una estructura nueva.
3. Todo commit debe explicar el **qué** y el **por qué**.
4. Terminado significa: **probado + documentado**.
