# 🎮 Minecraft Mods — Colección de Modding Java

## Propósito
Centralizar el código fuente, recursos y binarios empaquetados (`.jar`) de las modificaciones desarrolladas para Minecraft Java Edition.

## Incluye
- `escuela_mod/`: Código fuente y assets del mod temático de escuela (Fabric / Java).
- `isaac_mod/`: Código fuente, mecánicas roguelike y sinergias inspiradas en The Binding of Isaac.
- `villa_argentina_mod/`: Código fuente, estructuras personalizadas, entidades y elementos criollos/argentinos.
- Binarios listos para jugar en la carpeta `.minecraft/mods`:
  - `escuela-1.0.0.jar`
  - `isaac_roguelike-1.0.0.jar`
  - `villaargentina-1.0.0.jar`

## No incluye
- Caché pesada de compilación de Gradle (`build/loom-cache`, `.gradle/`).

## Mods de la Colección

| Mod | Archivo JAR | Descripción |
|---|---|---|
| **Escuela Mod** | `escuela-1.0.0.jar` | Aulas, mobiliario escolar, útiles y mecánicas educativas |
| **Isaac Roguelike** | `isaac_roguelike-1.0.0.jar` | Mazmorras con habitaciones procedurales, lágrimas, jefes e ítems pasivos |
| **Villa Argentina** | `villaargentina-1.0.0.jar` | Ambientación y cultura local argentina (bloques, comidas típicas, biomas) |

## Instalación en Minecraft
1. Instalar **Fabric Loader** para la versión correspondiente de Minecraft Java.
2. Copiar los archivos `.jar` a tu carpeta `%appdata%\.minecraft\mods`.
3. Iniciar el juego desde el lanzador de Minecraft con el perfil de Fabric.

## Relación con el Workspace MÖLDEA
- [AGENTS.md](../../../AGENTS.md)
- [proyectos/gaming/README.md](../README.md)
- [docs/GUIA_DOCUMENTACION.md](../../../docs/GUIA_DOCUMENTACION.md)
