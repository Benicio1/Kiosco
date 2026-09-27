# Smart TV Launcher — MÖLDEA Ultra-Lightweight TV OS

> **PROPÓSITO DEL PROYECTO:**  
> Transformar cualquier televisor tradicional conectado a una PC o Netbook de bajos recursos (ej. 4 GB de RAM con Windows 11) en una Smart TV de alta velocidad, con interfaz de 10 pies (10-foot UI), consumo mínimo de memoria (< 40 MB) y control remoto táctil desde el teléfono celular por Wi-Fi.

---

## ⚡ Características Principales

1. **Ultra-Bajo Consumo de RAM y GPU:**
   - Construido en arquitectura web pura nativa (HTML5/CSS3/Vanilla JS) acelerada por hardware.
   - Sin dependencias pesadas de terceros ni frameworks inflados (Cero Electron).
   - Ocupa menos de 40 MB de memoria en el servidor y alrededor de 70 MB en pantalla.

2. **Control Remoto Móvil desde el Celular (`/remote`):**
   - No requiere instalar ninguna app en el teléfono: se abre directamente en el navegador del celular.
   - Incluye código QR en la pantalla de la TV para conectarse en 1 segundo.
   - **D-Pad de Sala:** Arriba, Abajo, Izquierda, Derecha, OK central.
   - **Buscador de Teclado:** Escribí desde el celular y enviá el texto directamente a YouTube o a la TV sin lidiar con teclados en pantalla.
   - **Touchpad Virtual:** Deslizá el dedo por el celular para mover el cursor en la tele con clic táctil.
   - **Control de Volumen:** Modifica el volumen de la app y envía comandos de hardware a Windows 11.
   - **Feedback Háptico:** Vibración táctil en el teléfono en cada pulsación.

3. **Monitor de Memoria RAM en Vivo:**
   - La pantalla de la TV muestra en tiempo real cuánta memoria RAM libre y usada tiene la Netbook, para garantizar que nunca se congele.

4. **Modo Turbo para Windows 11 (`INICIAR_TURBO.bat`):**
   - Suspende procesos pesados en segundo plano (OneDrive, asistentes, widgets).
   - Limpia la memoria RAM en espera antes de arrancar.
   - Abre la interfaz en modo Kiosco a pantalla completa en Edge o Chrome.

---

## 🚀 Cómo Iniciar en la Tele

1. Entrá a esta carpeta en la Netbook:
   `c:\Users\Benicio\Documents\Antigravity organizado por el programa\proyectos\desktop\smart-tv-launcher`
2. Hacé doble clic en cualquiera de estos dos accesos:
   - **`scripts\INICIAR_TURBO.bat`** *(Recomendado para 4 GB de RAM)*: Optimiza la memoria y abre la TV.
   - **`scripts\INICIAR_SMART_TV.bat`**: Inicio estándar rápido.
3. En la tele aparecerá el código QR. Escanealo con la cámara de tu celular para tener el control remoto en la mano.

---

## 🛑 Cómo Cerrar

Hacé doble clic en **`scripts\CERRAR_SMART_TV.bat`** o cerrá la ventana del navegador.

---

## 📂 Estructura de Archivos

```
smart-tv-launcher/
├── public/
│   ├── index.html       # Pantalla 10-foot UI para la Tele
│   ├── tv.css           # Estilos cinematográficos de alto contraste
│   ├── tv.js            # Lógica de navegación, reloj y monitor RAM
│   ├── remote.html      # Interfaz web del Control Remoto para el Celular
│   ├── remote.css       # Estilo táctil oscuro con botones grandes
│   └── remote.js        # Gestos, touchpad, buscador y comandos SSE
├── scripts/
│   ├── INICIAR_TURBO.bat       # Lanzador con optimizador de RAM
│   ├── INICIAR_SMART_TV.bat    # Lanzador estándar
│   ├── CERRAR_SMART_TV.bat     # Detención limpia de procesos
│   └── optimizar_windows.ps1   # Script PowerShell de limpieza
├── tests/
│   └── tv.test.mjs             # Pruebas automatizadas de endpoints
├── server.mjs                  # Servidor HTTP y SSE ultraliviano (Node.js)
├── package.json
└── README.md
```

---

## 🧪 Pruebas Automatizadas

```bash
npm test
```
*7/7 pruebas unitarias y de integración superadas.*

---

## 🔗 Relación con el Contrato Canónico
- [AGENTS.md](../../../AGENTS.md)
- [docs/GUIA_DOCUMENTACION.md](../../../docs/GUIA_DOCUMENTACION.md)
