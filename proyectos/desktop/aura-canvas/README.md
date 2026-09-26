# AuraPaint Pro Studio — Arquitectura y MVP de Ilustración Digital

Bienvenido a la implementación técnica y prototipo funcional de **AuraPaint**, un motor de dibujo e ilustración digital de alto rendimiento para escritorio diseñado bajo los estándares de ingeniería de **Clip Studio Paint** e **ibis Paint X**.

---

## 🚀 Cómo Ejecutar el MVP Inmediatamente (Windows)

El ejecutable nativo ya está compilado y listo en la raíz del proyecto:
- **Archivo:** `AuraCanvasMVP.exe`
- **Compilación manual (opcional si deseas modificarlo):**
  ```powershell
  & "C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe" /target:winexe /optimize+ /r:System.Windows.Forms.dll /r:System.Drawing.dll /out:AuraCanvasMVP.exe AuraCanvasMVP.cs
  ```
- **Lanzamiento:** Haz doble clic sobre `AuraCanvasMVP.exe` o ejecútalo desde la terminal:
  ```powershell
  .\AuraCanvasMVP.exe
  ```

---

## 🎨 Nuevas Funcionalidades y Mejoras Integradas

### 1. Cambio de Tamaño de Lienzo & Presets (Menú Lienzo / Botón Dock / Ctrl+N)
- **Presets Profesionales incluidos:**
  * **A4 - 300 DPI (2480 × 3508 px)** — Estándar para impresión artística y cómic.
  * **A4 Manga - 600 DPI (4960 × 7016 px)** — Resolución profesional para tramas de imprenta.
  * **4K Ultra HD (3840 × 2160 px)** — Ilustración digital para pantallas de alta densidad.
  * **Full HD 1080p (1920 × 1080 px)** — Dibujo ágil y bocetos rápidos.
  * **Cuadrado Redes (2048 × 2048 px)** — Formato optimizado para Instagram / ArtStation.
  * **Banner Web / Twitter (3000 × 2000 px)**.
  * **Medidas Personalizadas:** Ajuste libre de ancho y alto en píxeles con opción de mantener trazos o crear lienzo nuevo en blanco.

### 2. Navegación Fluida Total (Paneo y Zoom 100% Funcionales)
- **Paneo con Rueda / Botón Central:** Mantén presionado el botón central del ratón y arrastra.
- **Paneo con Tecla Espacio:** Mantén pulsada la tecla `Espacio` y arrastra con el botón izquierdo (estilo Photoshop / Clip Studio).
- **Paneo con Clic Derecho:** Puedes arrastrar directamente con el botón secundario del ratón.
- **Zoom Continuo Centrado:** Rueda del ratón hacia arriba/abajo (centrado exactamente bajo el puntero).
- **Ajustar a Pantalla:** `Ctrl + 0` o menú Lienzo.
- **Rotar Lienzo:** Tecla `R` (gira 15° sucesivamente sin perder calidad de píxel).

### 3. Selector de Pinceles Idéntico a ibis Paint X
Al pulsar el botón **`[ 🖌️ Pluma (Fuerte) ▼ ]`**, se abre el selector con el diseño y la distribución exacta de **ibis Paint X**:
- **Barra superior:** Pincel (25), pestañas de `Básico`, `Personalizado`, `En línea`.
- **Panel lateral izquierdo (Categorías verticales):** `📊 Todos`, `📈 Básico`, `✏️ Bosquejo`, `📖 Cómic`, `🖋️ Tinta`, `💨 Aerógrafo`, `💧 Acuarela`, `🎨 Textura`, `🖌️ Pintar`, `🧹 Gomas`.
- **Lista central interactiva:**
  * **Muestra de trazo ondulado dinámico (`~` S-Curve):** Con desvanecimiento y variación cónica de grosor en tiempo real según el tipo de punta.
  * Nombre del pincel y tamaño predeterminado (`17.5`, `30.0`, `48.0`, `80.0`, etc.).
  * Resaltado celeste suave (`#D7EBF8`) al seleccionar.
- **Inspector lateral derecho:**
  * **Caja de previsualización en Tablero de Ajedrez (Checkerboard):** Muestra el trazo en vivo renderizado con tu color actual.
  * Deslizadores con valores numéricos y porcentaje: **Grosor (1 a 250 px)** y **Opacidad (5% a 100%)**.
  * Botón de confirmación: `[ ✔ Seleccionar este Pincel ]`.

### 4. Cargador Universal de Imágenes de Referencia (Sin Error "El parámetro no es válido")
- **Soporte completo de formatos:** Ahora carga sin fallos archivos **JPG, JPEG, JFIF, PNG, GIF, BMP, WebP y TIFF**.
- **Carga desacoplada de memoria:** Resuelve el error de Windows GDI+ al cargar JPGs progresivos, fotos con perfiles EXIF o imágenes descargadas de la web.
- **Arrastrar y Soltar (Drag & Drop):** Puedes arrastrar directamente cualquier imagen desde el explorador de archivos de Windows y soltarla dentro del recuadro de referencia.
- **Navegación y Cuentagotas:** Rueda para zoom, arrastre para encuadre y `Shift + Clic` para copiar el color del píxel al instante.

### 6. Lienzo Limpio (Sin HUD y Sin Cuadrícula)
- **Eliminación del HUD inferior derecho:** La caja de telemetría fue completamente removida para dejar el lienzo 100% despejado.
- **Sin cuadrícula por defecto:** La cuadrícula solo aparecerá si la activas expresamente en el menú **`Ver -> Mostrar Cuadrícula de Guía`**. Además, la base del lienzo elimina cualquier línea residual entre mosaicos.

---

## 📁 Estructura del Repositorio

- **`AuraCanvasMVP.exe`**: Binario nativo de Windows funcional con ventana, captura de lápiz de baja latencia, HUD de telemetría y renderizado por mosaicos.
- **`AuraCanvasMVP.cs`**: Código fuente C# Win32 / Pointer API del MVP.
- **`ARCHITECTURE.md`**: Documento exhaustivo del diseño del sistema, análisis comparativo de stacks, pipeline de GPU, shaders, memoria virtual y algoritmos.
- **`include/AuraCore.hpp`**: Núcleo modular en C++20 (Catmull-Rom spline stabilizer, Sparse Tile Map, TileDiff Undo Journal).
- **`shaders/brush_dab.wgsl`**: Compute Shader de estampado masivo de sellos de pincel acelerado por GPU (DirectX 12 / Metal / Vulkan).
- **`shaders/layer_composite.wgsl`**: Fragment Shader con modos de fusión (Multiply, Screen, Overlay, Soft Light, Color Dodge) y soporte para Máscaras de Recorte.

---

## 🔗 Relación con el Workspace MÖLDEA
- [AGENTS.md](../../../AGENTS.md)
- [proyectos/desktop/README.md](../README.md)
- [docs/GUIA_DOCUMENTACION.md](../../../docs/GUIA_DOCUMENTACION.md)

