# Guía de Documentación — Antigravity full organizado

> **Objetivo:** que cualquier persona o IA pueda continuar el proyecto sin preguntar dónde documentar cada cosa. Esta guía complementa [AGENTS.md](../AGENTS.md) y los README de cada módulo.

## 1. Autoridad de las reglas

1. AGENTS.md define el contrato general, límites de riesgo, arquitectura, versionado y obligación de documentar.
2. El README.md de cada carpeta define el uso concreto de ese módulo.
3. Esta guía define dónde poner cada documento y cómo redactarlo.
4. Si dos reglas chocan, no se inventa una tercera: se informa el conflicto y se actualiza la regla superior.

## Versionado obligatorio (SemVer)

Todo proyecto nuevo comienza en **1.0.0**. A partir de ahí:

- **MAJOR** (1.0.0 → 2.0.0): cambio grande, ruptura de compatibilidad o rediseño estructural.
- **MINOR** (1.0.0 → 1.1.0): funcionalidad, pantalla, módulo o capacidad nueva compatible.
- **PATCH** (1.0.0 → 1.0.1): bug, texto, estilo o ajuste pequeño sin cambiar el contrato.

Cada versión formal se registra en docs/HITOS.md. Cuando el proyecto tenga esos archivos, la versión debe coincidir en package.json, package-lock.json y .scaffold.json.

## 2. Regla de oro para carpetas nuevas

Cuando una tarea necesite una carpeta nueva:

1. Revisar el README de la carpeta padre.
2. Confirmar que la carpeta no duplica otra existente.
3. Crear la carpeta y su README.md en el mismo cambio.
4. En ese README indicar propósito, alcance, exclusiones, archivos principales, responsable y enlaces relacionados.
5. Agregar la carpeta al índice del módulo padre si existe.
6. Registrar la decisión en docs/ESTADO_ACTUAL.md; si cambia la arquitectura, crear un ADR.

### Plantilla mínima de README de módulo

~~~markdown
# ruta/de/la-carpeta/ — Nombre del módulo

## Propósito
Qué problema resuelve esta carpeta.

## Incluye
- Archivos y tipos de contenido permitidos.

## No incluye
- Contenido que debe ir en otra carpeta.

## Archivos principales
| Archivo | Función |
|---|---|
| ... | ... |

## Reglas de cambio
- Pruebas, seguridad, versionado y enlaces relevantes.

## Relación con el proyecto
- [AGENTS.md](../AGENTS.md)
- [Guía de documentación](./GUIA_DOCUMENTACION.md)
~~~

## 3. Tipos de documentos

- **README.md:** explica una carpeta o el proyecto a una persona nueva; no es un diario de trabajo.
- **PLAN.md:** qué se quiere construir y qué falta.
- **ESTADO_ACTUAL.md:** qué quedó funcionando al cerrar la sesión.
- **BUGS.md:** errores técnicos no triviales ya resueltos.
- **ADR:** decisiones que afectan estructura, stack, seguridad o distribución.
- **Guías:** procedimientos repetibles para instalar, usar, probar, desplegar o auditar.
- **HITOS.md:** entregas y cambios de versión.

## 4. Estándar de redacción

- Escribir en español claro, con nombres técnicos en inglés solo cuando sean necesarios.
- Explicar primero qué resuelve algo y después cómo funciona.
- Usar rutas relativas en enlaces Markdown.
- Evitar instrucciones duplicadas: enlazar al documento canónico.
- No guardar secretos, tokens ni datos personales en documentación.
- No editar manualmente dist/; documentar el cambio en src/ y regenerar la distribución.

## 5. Roles de trabajo

- **Antigravity:** desarrolla, crea carpetas y mantiene los documentos del cambio.
- **Claude:** audita seguridad, coherencia y cumplimiento de estas reglas.
- **Codex:** realiza revisión independiente, pruebas y control de regresiones.

## 6. Cierre obligatorio

Una tarea está terminada solo cuando el código está probado, la documentación está actualizada y cada carpeta nueva tiene su README.
