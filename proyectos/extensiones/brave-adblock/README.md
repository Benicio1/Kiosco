# 🛡️ Aegis AdBlock - Extensión para Brave

Un bloqueador de anuncios y rastreadores moderno, ultraligero y de alto rendimiento basado en **Manifest V3** diseñado específicamente para el navegador **Brave** (y compatible con cualquier navegador basado en Chromium).

---

## 🚀 Características principales

- **Bloqueo a nivel de red (declarativeNetRequest MV3):** Intercepta y detiene solicitudes antes de que se descarguen (Google Ads, DoubleClick, Taboola, Outbrain, Criteo, PopAds, trackers de telemetría y redes de afiliados).
- **Filtrado cosmético en tiempo real (`cosmetic.css` y `content.js`):** Oculta espacios vacíos, banners flotantes molestos, popups invasivos y elementos patrocinados inyectados dinámicamente.
- **Salto y aceleración de anuncios en YouTube:** Detecta y presiona automáticamente el botón de "Saltar anuncio" y adelanta clips publicitarios en reproductores HTML5.
- **Interfaz emergente moderna (Popup):**
  - Interruptor maestro de encendido/apagado general.
  - Botón para pausar/activar la protección en el sitio web actual (Lista Blanca / Whitelist).
  - Contador en tiempo real de elementos bloqueados en la pestaña activa.
  - Contador acumulado de anuncios bloqueados.
  - Indicador numérico (Badge) en el icono de la barra de extensiones.

---

## 📦 Cómo instalarla en Brave paso a paso

1. Abre tu navegador **Brave**.
2. En la barra de direcciones, escribe:
   ```text
   brave://extensions
   ```
   *(o ve a Menú ☰ > Extensiones).*
3. En la esquina superior derecha de la página de extensiones, **activa el interruptor "Modo de desarrollador"** (*Developer mode*).
4. Haz clic en el botón **"Cargar descomprimida"** (*Load unpacked*).
5. Selecciona la carpeta donde se encuentra este proyecto:
   ```text
   C:\Users\Benicio\Documents\Antigravity organizado por el programa\proyectos\extensiones\brave-adblock
   ```
6. ¡Listo! Verás el escudo de **Aegis AdBlock** instalado. Para tenerlo siempre a mano, haz clic en el icono del rompecabezas de extensiones en la barra de Brave y selecciona el pin 📌 para fijarlo.

---

## 📂 Estructura del Proyecto

```text
brave-adblock/
├── manifest.json            # Configuración principal de la extensión (Manifest V3)
├── background.js           # Service Worker: contadores, badge e integración con la lista blanca
├── rules/
│   └── ad_rules.json       # Reglas declarativas de bloqueo de red (100+ filtros de dominios y patrones)
├── content/
│   ├── content.js          # Observador del DOM, eliminación dinámica y salto de anuncios en video
│   └── cosmetic.css        # Reglas CSS para ocultar contenedores y banners
├── popup/
│   ├── popup.html          # Interfaz de usuario del menú emergente
│   ├── popup.css           # Estilos oscuros y modernos
│   └── popup.js            # Lógica interactiva de estadísticas y whitelist
├── icons/
│   ├── icon16.png
│   ├── icon48.png
│   └── icon128.png
├── generate_rules.js       # Script Node.js auxiliar para regenerar o expandir reglas
├── generate_icons.js       # Script Node.js que genera los iconos PNG
└── README.md               # Esta guía
```

---

## ⚙️ Cómo agregar más dominios o reglas

Si quieres añadir más servidores publicitarios a la lista:
1. Abre `generate_rules.js`.
2. Agrega los dominios o patrones deseados en el arreglo `domains` o `patterns`.
3. Ejecuta en la terminal:
   ```bash
   node generate_rules.js
   ```
4. En `brave://extensions`, haz clic en el botón de recarga 🔄 sobre la tarjeta de la extensión.

---

## 🔗 Relación con el Workspace MÖLDEA
- [AGENTS.md](../../../AGENTS.md)
- [proyectos/extensiones/README.md](../README.md)
- [docs/GUIA_DOCUMENTACION.md](../../../docs/GUIA_DOCUMENTACION.md)

