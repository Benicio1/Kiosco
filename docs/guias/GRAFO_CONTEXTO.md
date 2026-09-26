# Grafo de Contexto — Understand-Anything

> **QUÉ ES:** Un mapa navegable del código de **Antigravity full organizado** (archivos, funciones, clases, relaciones) generado automáticamente por [Understand-Anything](https://github.com/Egonex-AI/Understand-Anything) (MIT, open source). No reemplaza a `docs/ESTADO_ACTUAL.md`: el parte de turno dice **qué se está haciendo**, el grafo muestra **cómo está armado** el código.

---

## 1. Por qué existe

Sin esto, cada IA (o cada sesión nueva) tiene que releer todo `src/` para entender la arquitectura. El grafo se genera una vez y cualquier asistente lo consulta en segundos — Claude Code, Codex, Gemini CLI / Antigravity y OpenCode lo soportan todos (es agnóstico de cuál IA lo corre).

## 2. Cuándo regenerarlo

`node src/index.mjs revisar` avisa solo cuando corresponde. Se considera **desactualizado** si:
- Nunca se generó.
- Pasaron más de **14 días** desde la última generación.
- Hubo más de **15 commits** desde la última generación.
- Hay un cambio estructural grande reciente (carpeta nueva, módulo eliminado) — esto lo evalúa criterio humano o de la IA, no un contador.

## 3. Cómo generarlo

**Si tu asistente ya lo tiene instalado** (Claude Code, Codex, Gemini CLI/Antigravity, OpenCode con el plugin Understand-Anything):
```
/understand
```
Para ver el resultado en un dashboard visual:
```
/understand-dashboard
```
o, sin depender de ningún asistente en particular:
```bash
npx understand-anything-viewer .
```

**Si tu compañero NO lo tiene instalado todavía:** correr el instalador de una línea del propio proyecto (ver [understand-anything.com](https://understand-anything.com/) o el README del repo oficial) según su herramienta (Codex, Gemini CLI, Copilot CLI, etc.). Claude Code y Cursor lo auto-descubren vía plugin.

## 4. Después de generarlo: sellar la fecha

El grafo en sí (`.ua/knowledge-graph.json`) **no se versiona en git** (pesa, y queda viejo apenas cambia el código — versionarlo daría falsa sensación de estar al día). Lo que sí se versiona es la marca de "cuándo fue la última vez":

```bash
node src/index.mjs grafo marcar
```

Esto graba en `.scaffold.json` la fecha y el commit actual. Sin este paso, `moldea revisar` va a seguir pidiendo regenerar aunque ya lo hayas hecho.

## 5. Distribución a compañeros (plan del `.exe`)

Cuando el scaffolder se empaquete como `scaffolder.exe` (ver `docs/PLAN.md`, Fase 3), al primer uso debe detectar si Understand-Anything está instalado en la máquina y, si no, ofrecer correr su instalador — así un compañero sin nada configurado puede sumarse al mismo flujo de contexto sin pasos manuales.
