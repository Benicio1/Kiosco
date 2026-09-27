# Plan de Trabajo y Requerimientos — Antigravity full organizado

> **CÓMO SE USA ESTE ARCHIVO:**  
> Guarda la visión inicial del proyecto y el listado de tareas a corto y mediano plazo en formato checklist (`- [ ]` pendiente, `- [x]` completado).

---

## 🎯 1. Visión y Requerimientos Iniciales
Centralizar y estructurar de manera modular y canónica el ecosistema completo de desarrollos y proyectos personales/profesionales de Antigravity (Webs, Aplicaciones de Escritorio en C#, Extensiones Chromium, Mods de Videojuegos y Drivers de Sistema), asegurando que cada proyecto cumpla con los límites de acoplamiento, documentación y portabilidad exigidos por MÖLDEA v1.0.0.

---

## 🧭 2. Diagnóstico de Destino, Integración y Protección (Completado por la IA en Onboarding)
* **Tipo de Producto / Software:** Hub Multi-Proyecto / Monorepo Modular organizado por categorías.
* **Formato de Entrega y Portabilidad:** Mixto según el módulo (PWA Web, Binarios Portables `.exe` sin instalación, Extensiones `.zip`/unpacked, JARs de Minecraft).
* **Plan de Integración Futura:** Cada módulo es autónomo con documentación interoperable.
* **Estrategia de Protección / IP:** Proyectos locales versionados; exclusión estricta de cachés de compilación pesadas y artefactos transitorios en `.gitignore`.
* **Propuesta de Stack Tecnológico:** Multi-stack según el dominio (`proyectos/web`: Next.js/React/Prisma; `proyectos/desktop`: C# .NET/WinForms/Win32/GLSL; `proyectos/extensiones`: JavaScript Manifest V3; `proyectos/gaming`: Java Fabric).

---

## 📋 3. Checklist de Tareas
- [x] Inicialización de estructura base con MÖLDEA Scaffolder.
- [x] Creación del hub centralizado [`proyectos/`](../proyectos/README.md) con categorías (`web/`, `desktop/`, `extensiones/`, `gaming/`, `drivers/`).
- [x] Migración y organización segura de `gastro-stock-pwa` (Next.js 14 + Prisma + SQLite).
- [x] Migración y organización segura de `aura-canvas` (C# AuraPaint Studio con shaders y presets).
- [x] Migración y organización segura de `winclean-lite` (C# Optimizador de Windows 10/11 con UAC y Win32).
- [x] Migración y organización segura de `brave-adblock` (Extensión Aegis AdBlock Manifest V3).
- [x] Migración y organización segura de `minecraft-mods` (Escuela, Isaac Roguelike, Villa Argentina).
- [x] Migración y organización de `atheros-ar9271` (Driver y script de instalación automatizado).
- [x] Creación de `README.md` estandarizado para cada módulo y categoría con enlaces canónicos a [AGENTS.md](../AGENTS.md) y [docs/GUIA_DOCUMENTACION.md](./GUIA_DOCUMENTACION.md).
- [x] Configuración de `.gitignore` para ignorar cachés pesadas (`.next/`, `.gradle/`, `bin/`, `obj/`, `node_modules/`).
- [x] Desarrollo de la aplicación móvil "Organizador de Faltantes del Kiosco":
  - [x] Arquitectura limpia y modular (`src/core/`, `src/adapters/`, `src/gui/`).
  - [x] Carga exacta de los 9 productos solicitados (promo de panchos, sanchuchitos de miga, alfajores Guaymallén, alfajores Fulbito, juguitos Baggio multifruta, picodulces, chupetín con chicle, Flynn Paff, palitos de la selva) sin ningún extra genérico.
  - [x] Exclusión total de precios a pedido explícito del usuario.
  - [x] Funcionalidad de tachar / destachar con feedback táctil.
  - [x] Control de cantidades ágil con botones `+` y `-`.
  - [x] Capacidad de añadir nuevos artículos faltantes y editar/eliminar existentes.
  - [x] Filtros por estado ("Faltan", "Todos", "En Stock / Listos") y buscador en vivo insensible a acentos.
  - [x] Generador y formateador de lista para enviar o copiar a WhatsApp.
  - [x] Persistencia automática en LocalStorage del celular / navegador.
  - [x] Suite de 17 pruebas unitarias y de arquitectura con Node.js Test Runner al 100%.
- [x] Desarrollo de `smart-tv-launcher` (Smart TV de bajo consumo para tele conectada a PC):
  - [x] Interfaz 10-foot UI para la TV en HTML5/CSS nativo acelerado por hardware (< 40 MB RAM).
  - [x] Monitor de memoria RAM en tiempo real de la netbook integrado en la barra superior.
  - [x] Control Remoto táctil para celular vía Wi-Fi (`/remote`) con D-Pad, búsqueda por teclado, accesos directos, volumen y touchpad virtual.
  - [x] Servidor de eventos SSE en tiempo real sin dependencias externas en Node.js puro.
  - [x] Scripts de Modo Turbo para Windows 11 (`INICIAR_TURBO.bat` y `optimizar_windows.ps1`).
  - [x] Suite de 7 pruebas automatizadas (`tv.test.mjs`) superadas al 100%.
